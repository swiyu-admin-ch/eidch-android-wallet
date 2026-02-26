package ch.admin.foitt.wallet.platform.database.data

import androidx.room.withTransaction
import ch.admin.foitt.wallet.platform.database.data.dao.ActivityActorDisplayEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.ActivityActorDisplayWithImageDao
import ch.admin.foitt.wallet.platform.database.data.dao.ActivityClaimEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.ActivityWithDetailsDao
import ch.admin.foitt.wallet.platform.database.data.dao.ActivityWithDisplaysDao
import ch.admin.foitt.wallet.platform.database.data.dao.BatchRefreshDataDao
import ch.admin.foitt.wallet.platform.database.data.dao.BundleItemEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.BundleItemWithKeyBindingDao
import ch.admin.foitt.wallet.platform.database.data.dao.ClientAttestationDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialActivityEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimClusterDisplayEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimClusterEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimDisplayDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialDisplayDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialIssuerDisplayDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialKeyBindingEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.DaoProvider
import ch.admin.foitt.wallet.platform.database.data.dao.DeferredCredentialDao
import ch.admin.foitt.wallet.platform.database.data.dao.DeferredCredentialWithDisplaysDao
import ch.admin.foitt.wallet.platform.database.data.dao.EIdRequestCaseDao
import ch.admin.foitt.wallet.platform.database.data.dao.EIdRequestCaseWithStateDao
import ch.admin.foitt.wallet.platform.database.data.dao.EIdRequestFileDao
import ch.admin.foitt.wallet.platform.database.data.dao.EIdRequestStateDao
import ch.admin.foitt.wallet.platform.database.data.dao.ImageEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.RawCredentialDataDao
import ch.admin.foitt.wallet.platform.database.data.dao.VerifiableCredentialDao
import ch.admin.foitt.wallet.platform.database.data.dao.VerifiableCredentialWithBundleItemsWithKeyBindingDao
import ch.admin.foitt.wallet.platform.database.data.dao.VerifiableCredentialWithDisplaysAndClustersDao
import ch.admin.foitt.wallet.platform.database.domain.model.ChangeDatabasePassphraseError
import ch.admin.foitt.wallet.platform.database.domain.model.CreateDatabaseError
import ch.admin.foitt.wallet.platform.database.domain.model.DatabaseError
import ch.admin.foitt.wallet.platform.database.domain.model.DatabaseState
import ch.admin.foitt.wallet.platform.database.domain.model.OpenDatabaseError
import ch.admin.foitt.wallet.platform.database.domain.repository.DatabaseRepository
import ch.admin.foitt.wallet.platform.di.IoDispatcher
import ch.admin.foitt.wallet.platform.di.IoDispatcherScope
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.TestOnly
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DatabaseWrapper @Inject constructor(
    @param:IoDispatcherScope private val coroutineScope: CoroutineScope,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val databaseInitializer: DatabaseInitializer,
) : DatabaseRepository, DaoProvider {

    @TestOnly
    internal var mutexOwner: Any? = null
    private val databaseMutex = Mutex()
    private val database: MutableStateFlow<AppDatabase?> = MutableStateFlow(null)

    override val databaseState = database.map { currentDatabase ->
        when {
            currentDatabase != null && currentDatabase.isOpen && currentDatabase.tryDecrypt().isOk -> DatabaseState.OPEN
            else -> DatabaseState.CLOSED
        }
    }.stateIn(
        coroutineScope,
        SharingStarted.Eagerly,
        initialValue = DatabaseState.CLOSED,
    )

    override suspend fun createDatabase(
        passphrase: ByteArray,
    ): Result<Unit, CreateDatabaseError> = withContext(ioDispatcher) {
        databaseMutex.withLock(mutexOwner) {
            coroutineBinding {
                val createdDatabase = databaseInitializer.create(passphrase).bind()
                // The database is not actually written as long as we don't do anything with it.
                createdDatabase.tryDecrypt().mapError { error ->
                    createdDatabase.close()
                    DatabaseError.SetupFailed(error.throwable)
                }.bind()

                database.update { createdDatabase }
            }
        }
    }

    override suspend fun close() = withContext(ioDispatcher) {
        databaseMutex.withLock(mutexOwner) {
            Timber.d("Close database")
            database.value?.close()
            database.update { null }
        }
    }

    override suspend fun open(
        passphrase: ByteArray,
    ): Result<Unit, OpenDatabaseError> = withContext(ioDispatcher) {
        databaseMutex.withLock(mutexOwner) {
            Timber.d("Try to open database")
            coroutineBinding {
                if (database.value != null) Err(DatabaseError.AlreadyOpen).bind<OpenDatabaseError>()
                val createdDatabase = databaseInitializer.create(passphrase).bind()

                createdDatabase.tryDecrypt()
                    .onSuccess {
                        database.update { createdDatabase }
                    }.onFailure {
                        createdDatabase.close()
                        database.update { null }
                    }.bind()
            }
        }
    }

    override suspend fun checkIfCorrectPassphrase(
        passphrase: ByteArray
    ): Result<Unit, OpenDatabaseError> = withContext(ioDispatcher) {
        databaseMutex.withLock(mutexOwner) {
            coroutineBinding {
                val createdDatabase = databaseInitializer.create(passphrase).bind()
                createdDatabase.tryDecrypt()
                    .onFailure {
                        // do not close the current instance at this point
                        Timber.d("wrong passphrase")
                    }.bind()
                createdDatabase.close()
            }
        }
    }

    override suspend fun changePassphrase(newPassphrase: ByteArray): Result<Unit, ChangeDatabasePassphraseError> =
        withContext(ioDispatcher) {
            databaseMutex.withLock(mutexOwner) {
                database.value?.changePassword(newPassphrase)
                    ?: Err(DatabaseError.ReKeyFailed(IllegalStateException("Database is not open")))
            }
        }

    override fun isOpen(): Boolean {
        return database.value != null
    }

    override suspend fun <V> runInTransaction(block: suspend () -> V): V? = database.value?.withTransaction(block)

    private fun <T> getDaoFlow(mapper: (AppDatabase?) -> T?): StateFlow<T?> = database.map { mapper(it) }.stateIn(
        coroutineScope,
        SharingStarted.Eagerly,
        initialValue = null,
    )

    //region DAOs
    override val credentialDaoFlow: StateFlow<CredentialDao?> = getDaoFlow { it?.credentialDao() }
    override val verifiableCredentialDaoFlow: StateFlow<VerifiableCredentialDao?> = getDaoFlow {
        it?.verifiableCredentialDao()
    }
    override val verifiableCredentialWithDisplaysAndClustersDaoFlow:
        StateFlow<VerifiableCredentialWithDisplaysAndClustersDao?> = getDaoFlow {
            it?.verifiableCredentialWithDisplaysAndClustersDao()
        }
    override val verifiableCredentialWithBundleItemsWithKeyBindingDaoFlow:
        StateFlow<VerifiableCredentialWithBundleItemsWithKeyBindingDao?> = getDaoFlow { it?.credentialWithKeyBindingDao() }
    override val bundleItemEntityDaoFlow: StateFlow<BundleItemEntityDao?> = getDaoFlow { it?.bundleItemEntityDao() }
    override val bundleItemWithKeyBindingDaoFlow: StateFlow<BundleItemWithKeyBindingDao?> = getDaoFlow {
        it?.bundleItemWithKeyBindingDao()
    }
    override val deferredCredentialDao:
        StateFlow<DeferredCredentialDao?> = getDaoFlow { it?.deferredCredentialDao() }
    override val deferredCredentialWithDisplaysDao: StateFlow<DeferredCredentialWithDisplaysDao?> = getDaoFlow {
        it?.deferredCredentialWithDisplaysDao()
    }
    override val credentialDisplayDaoFlow: StateFlow<CredentialDisplayDao?> = getDaoFlow { it?.credentialDisplayDao() }
    override val credentialClaimDaoFlow: StateFlow<CredentialClaimDao?> = getDaoFlow { it?.credentialClaimDao() }
    override val credentialClaimDisplayDaoFlow: StateFlow<CredentialClaimDisplayDao?> =
        getDaoFlow { it?.credentialClaimDisplayDao() }
    override val credentialIssuerDisplayDaoFlow: StateFlow<CredentialIssuerDisplayDao?> =
        getDaoFlow { it?.credentialIssuerDisplayDao() }
    override val credentialClaimClusterEntityDao: StateFlow<CredentialClaimClusterEntityDao?> = getDaoFlow {
        it?.credentialClaimClusterEntityDao()
    }
    override val credentialClaimClusterDisplayEntityDao: StateFlow<CredentialClaimClusterDisplayEntityDao?> =
        getDaoFlow { it?.credentialClaimClusterDisplayEntityDao() }
    override val credentialKeyBindingEntityDaoFlow: StateFlow<CredentialKeyBindingEntityDao?> = getDaoFlow {
        it?.credentialKeyBindingEntityDao()
    }

    override val credentialActivityEntityDao: StateFlow<CredentialActivityEntityDao?> =
        getDaoFlow { it?.credentialActivityEntityDao() }
    override val activityClaimEntityDao: StateFlow<ActivityClaimEntityDao?> =
        getDaoFlow { it?.activityClaimEntityDao() }
    override val activityActorDisplayEntityDao: StateFlow<ActivityActorDisplayEntityDao?> = getDaoFlow {
        it?.activityActorDisplayEntityDao()
    }
    override val activityActorDisplayWithImageDao: StateFlow<ActivityActorDisplayWithImageDao?> = getDaoFlow {
        it?.activityActorDisplayWithImageDao()
    }
    override val activityWithDetailsDao: StateFlow<ActivityWithDetailsDao?> =
        getDaoFlow { it?.activityWithDetailsDao() }
    override val activityWithDisplaysDao: StateFlow<ActivityWithDisplaysDao?> =
        getDaoFlow { it?.activityWithDisplaysDao() }

    override val imageEntityDao: StateFlow<ImageEntityDao?> = getDaoFlow { it?.imageEntityDao() }

    override val eIdRequestCaseDaoFlow: StateFlow<EIdRequestCaseDao?> =
        getDaoFlow { it?.eIdRequestCaseDao() }
    override val eIdRequestStateDaoFlow: StateFlow<EIdRequestStateDao?> = getDaoFlow { it?.eIdRequestStateDao() }
    override val eIdRequestCaseWithStateDaoFlow: StateFlow<EIdRequestCaseWithStateDao?> = getDaoFlow {
        it?.eIdRequestCaseWithStateDao()
    }
    override val eIdRequestFileDaoFlow: StateFlow<EIdRequestFileDao?> = getDaoFlow { it?.eIdRequestFileDao() }
    override val rawCredentialDataDao: StateFlow<RawCredentialDataDao?> = getDaoFlow { it?.rawCredentialDataDao() }
    override val clientAttestationDaoFlow: StateFlow<ClientAttestationDao?> = getDaoFlow { it?.clientAttestationDao() }
    override val batchRefreshDataDao: StateFlow<BatchRefreshDataDao?> = getDaoFlow { it?.batchRefreshDataDao() }
    //endregion
}
