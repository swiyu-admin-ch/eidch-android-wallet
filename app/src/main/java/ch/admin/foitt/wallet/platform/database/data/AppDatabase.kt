@file:Suppress("TooManyFunctions")

package ch.admin.foitt.wallet.platform.database.data

import androidx.room.AutoMigration
import androidx.room.Dao
import androidx.room.Database
import androidx.room.RawQuery
import androidx.room.RoomDatabase
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import ch.admin.foitt.wallet.platform.database.data.AppDatabase.Companion.DATABASE_VERSION
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
import ch.admin.foitt.wallet.platform.database.data.migrations.Migration17to18
import ch.admin.foitt.wallet.platform.database.domain.model.ActivityActorDisplayEntity
import ch.admin.foitt.wallet.platform.database.domain.model.ActivityClaimEntity
import ch.admin.foitt.wallet.platform.database.domain.model.BatchRefreshDataEntity
import ch.admin.foitt.wallet.platform.database.domain.model.BundleItemEntity
import ch.admin.foitt.wallet.platform.database.domain.model.ClientAttestation
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialActivityEntity
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimClusterDisplayEntity
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimClusterEntity
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialIssuerDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialKeyBindingEntity
import ch.admin.foitt.wallet.platform.database.domain.model.DatabaseError
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialEntity
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestCase
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestFile
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestState
import ch.admin.foitt.wallet.platform.database.domain.model.ImageEntity
import ch.admin.foitt.wallet.platform.database.domain.model.RawCredentialData
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialEntity
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import net.zetetic.database.sqlcipher.SQLiteDatabase
import timber.log.Timber

@Database(
    entities = [
        Credential::class,
        VerifiableCredentialEntity::class,
        DeferredCredentialEntity::class,
        CredentialDisplay::class,
        CredentialClaim::class,
        CredentialClaimDisplay::class,
        CredentialIssuerDisplay::class,
        CredentialClaimClusterEntity::class,
        CredentialClaimClusterDisplayEntity::class,
        CredentialKeyBindingEntity::class,
        CredentialActivityEntity::class,
        ActivityClaimEntity::class,
        ActivityActorDisplayEntity::class,
        ImageEntity::class,
        EIdRequestCase::class,
        EIdRequestState::class,
        EIdRequestFile::class,
        RawCredentialData::class,
        ClientAttestation::class,
        BatchRefreshDataEntity::class,
        BundleItemEntity::class,
    ],
    version = DATABASE_VERSION,
    autoMigrations = [
        // Migration1to2 -> Schema 3.1 to 3.3
        // Migration2to3 -> Schema 3.3 to 3.4
        AutoMigration(from = 3, to = 4), // Schema 3.3 to 3.4
        AutoMigration(from = 4, to = 5), // Schema 3.5 to 3.6
        // Migration5to6 -> Schema 3.6 to 3.7
        // Migration6to7 -> Schema 3.7 to 3.8
        AutoMigration(from = 7, to = 8), // Schema 3.8. to 3.9
        AutoMigration(from = 8, to = 9), // Schema 3.9 to 3.10
        AutoMigration(from = 9, to = 10), // Schema 3.10 to 4.0
        AutoMigration(from = 10, to = 11), // Schema 4.0 to 4.1
        // Migration11to12 -> Schema 4.1 to 5.0
        AutoMigration(from = 12, to = 13), // Schema 5.0 to 6.0
        AutoMigration(from = 13, to = 14), // Schema 6.0 to 6.1
        // Migration14to15 -> No Schema change, Schema 6.1
        // Migration15to16 -> Schema 6.1 to 6.2
        // Migration16to17 -> Schema 6.2 to 6.3
        AutoMigration(from = 17, to = 18, spec = Migration17to18::class), // Schema 6.3 to 6.4
    ], // see also migrations in SqlCipherDatabaseInitializer
    exportSchema = true,
)
@Suppress("TooManyFunctions")
abstract class AppDatabase : RoomDatabase() {
    // each DAO must be defined as an abstract method
    abstract fun credentialDao(): CredentialDao
    abstract fun verifiableCredentialDao(): VerifiableCredentialDao
    abstract fun verifiableCredentialWithDisplaysAndClustersDao(): VerifiableCredentialWithDisplaysAndClustersDao
    abstract fun credentialWithKeyBindingDao(): VerifiableCredentialWithBundleItemsWithKeyBindingDao
    abstract fun bundleItemEntityDao(): BundleItemEntityDao
    abstract fun bundleItemWithKeyBindingDao(): BundleItemWithKeyBindingDao
    abstract fun deferredCredentialDao(): DeferredCredentialDao
    abstract fun deferredCredentialWithDisplaysDao(): DeferredCredentialWithDisplaysDao
    abstract fun credentialClaimDao(): CredentialClaimDao
    abstract fun credentialClaimDisplayDao(): CredentialClaimDisplayDao
    abstract fun credentialDisplayDao(): CredentialDisplayDao
    abstract fun credentialIssuerDisplayDao(): CredentialIssuerDisplayDao
    abstract fun credentialClaimClusterEntityDao(): CredentialClaimClusterEntityDao
    abstract fun credentialClaimClusterDisplayEntityDao(): CredentialClaimClusterDisplayEntityDao
    abstract fun credentialKeyBindingEntityDao(): CredentialKeyBindingEntityDao

    abstract fun credentialActivityEntityDao(): CredentialActivityEntityDao
    abstract fun activityClaimEntityDao(): ActivityClaimEntityDao
    abstract fun activityActorDisplayEntityDao(): ActivityActorDisplayEntityDao
    abstract fun activityActorDisplayWithImageDao(): ActivityActorDisplayWithImageDao
    abstract fun activityWithDetailsDao(): ActivityWithDetailsDao
    abstract fun activityWithDisplaysDao(): ActivityWithDisplaysDao

    abstract fun imageEntityDao(): ImageEntityDao

    abstract fun eIdRequestCaseDao(): EIdRequestCaseDao
    abstract fun eIdRequestStateDao(): EIdRequestStateDao
    abstract fun eIdRequestCaseWithStateDao(): EIdRequestCaseWithStateDao
    abstract fun eIdRequestFileDao(): EIdRequestFileDao
    abstract fun clientAttestationDao(): ClientAttestationDao

    abstract fun batchRefreshDataDao(): BatchRefreshDataDao

    abstract fun rawCredentialDataDao(): RawCredentialDataDao

    abstract fun decryptionTestDao(): DecryptionTestDao

    fun changePassword(newPassword: ByteArray): Result<Unit, DatabaseError.ReKeyFailed> =
        runSuspendCatching {
            val database = openHelper.writableDatabase as SQLiteDatabase
            database.changePassword(newPassword)
        }.mapError { throwable ->
            DatabaseError.ReKeyFailed(throwable)
        }

    suspend fun tryDecrypt(): Result<Unit, DatabaseError.WrongPassphrase> {
        return runSuspendCatching {
            decryptionTestDao().test()
            Unit
        }.mapError { throwable ->
            Timber.d(t = throwable, message = "error")
            DatabaseError.WrongPassphrase(throwable)
        }
    }

    @Dao
    interface DecryptionTestDao {
        // Returns an Int if database decryption was successful
        // https://www.zetetic.net/sqlcipher/sqlcipher-api/#testing-the-key
        @RawQuery
        suspend fun test(query: SupportSQLiteQuery = SimpleSQLiteQuery("SELECT count(*) FROM sqlite_master")): Int
    }

    companion object {
        internal const val DATABASE_VERSION = 18 // db scheme v6.4
    }
}
