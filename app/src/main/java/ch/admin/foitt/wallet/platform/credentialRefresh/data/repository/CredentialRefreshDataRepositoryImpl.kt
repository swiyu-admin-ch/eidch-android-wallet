package ch.admin.foitt.wallet.platform.credentialRefresh.data.repository

import ch.admin.foitt.openid4vc.domain.model.BatchSize
import ch.admin.foitt.openid4vc.domain.model.TokenType
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.model.CredentialRefreshDataError
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.repository.CredentialRefreshDataRepository
import ch.admin.foitt.wallet.platform.database.data.dao.BatchRefreshDataDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialAuthenticationDao
import ch.admin.foitt.wallet.platform.database.data.dao.DaoProvider
import ch.admin.foitt.wallet.platform.database.data.dao.DpopBindingDao
import ch.admin.foitt.wallet.platform.database.data.dao.VerifiableCredentialWithAuthenticationDao
import ch.admin.foitt.wallet.platform.database.data.dao.VerifiableCredentialWithBatchDataAndAuthenticationDao
import ch.admin.foitt.wallet.platform.database.domain.model.BatchRefreshDataEntity
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialAuthenticationEntity
import ch.admin.foitt.wallet.platform.database.domain.model.DpopBindingEntity
import ch.admin.foitt.wallet.platform.database.domain.usecase.RunInTransaction
import ch.admin.foitt.wallet.platform.di.IoDispatcher
import ch.admin.foitt.wallet.platform.utils.suspendUntilNonNull
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class CredentialRefreshDataRepositoryImpl @Inject constructor(
    daoProvider: DaoProvider,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val runInTransaction: RunInTransaction,
) : CredentialRefreshDataRepository {

    override suspend fun getCredentialRefreshDataById(credentialId: Long) = runSuspendCatching {
        withContext(ioDispatcher) {
            credentialRefreshDataDao().getById(credentialId)
        }
    }.mapError { throwable ->
        Timber.e(t = throwable, message = "Failed to get credential refresh data")
        CredentialRefreshDataError.Unexpected(throwable)
    }

    override suspend fun getAllCredentialRefreshData() = runSuspendCatching {
        withContext(ioDispatcher) {
            credentialRefreshDataDao().getAll()
        }
    }.mapError { throwable ->
        Timber.e(t = throwable, message = "Failed to get credentials refresh data")
        CredentialRefreshDataError.Unexpected(throwable)
    }

    override suspend fun getAllBatchCredentialRefreshData() = runSuspendCatching {
        withContext(ioDispatcher) {
            batchCredentialRefreshDataDao().getAll()
        }
    }.mapError { throwable ->
        Timber.e(t = throwable, message = "Failed to get batch credentials refresh data")
        CredentialRefreshDataError.Unexpected(throwable)
    }

    override suspend fun getBatchRefreshDataById(credentialId: Long) = runSuspendCatching {
        withContext(ioDispatcher) {
            batchRefreshDataDao().getByCredentialId(credentialId)
        }
    }.mapError { throwable ->
        Timber.e(t = throwable, message = "Failed to get batch refresh data")
        CredentialRefreshDataError.Unexpected(throwable)
    }

    override suspend fun getCredentialAuthenticationById(
        credentialId: Long,
    ): Result<CredentialAuthenticationEntity?, CredentialRefreshDataError> = runSuspendCatching {
        withContext(ioDispatcher) {
            credentialAuthenticationDao().getByCredentialId(credentialId)
        }
    }.mapError { throwable ->
        Timber.e(t = throwable, message = "Failed to get credential authentication data")
        CredentialRefreshDataError.Unexpected(throwable)
    }

    override suspend fun saveRefreshData(
        credentialId: Long,
        batchSize: BatchSize?,
        accessToken: String,
        refreshToken: String,
        dpopKeyBinding: KeyBinding?,
    ): Result<Long, CredentialRefreshDataError> = runSuspendCatching {
        withContext(ioDispatcher) {
            runInTransaction {
                batchSize?.let {
                    batchRefreshDataDao().insert(
                        BatchRefreshDataEntity(
                            credentialId = credentialId,
                            batchSize = batchSize,
                        )
                    )
                }

                val existingAuthentication =
                    credentialAuthenticationDao().getByCredentialId(credentialId)
                val authenticationId = credentialAuthenticationDao().insert(
                    CredentialAuthenticationEntity(
                        id = existingAuthentication?.id ?: 0,
                        credentialId = credentialId,
                        tokenType = existingAuthentication?.tokenType ?: TokenType.BEARER,
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                    )
                )

                if (dpopKeyBinding != null) {
                    dpopBindingDao().insert(
                        DpopBindingEntity(
                            id = dpopKeyBinding.identifier,
                            credentialAuthenticationId = authenticationId,
                            algorithm = dpopKeyBinding.algorithm.name,
                            bindingType = dpopKeyBinding.bindingType,
                            publicKey = dpopKeyBinding.publicKey,
                            privateKey = dpopKeyBinding.privateKey,
                        )
                    )
                }

                credentialId
            } ?: error("saveRefreshData: transaction failed")
        }
    }.mapError { throwable ->
        Timber.e(t = throwable, message = "Failed to save refresh data")
        CredentialRefreshDataError.Unexpected(throwable)
    }

    override suspend fun updateBatchSize(credentialId: Long, batchSize: BatchSize): Result<Int, CredentialRefreshDataError> {
        return runSuspendCatching {
            withContext(ioDispatcher) {
                batchRefreshDataDao().updateBatchSize(credentialId, batchSize)
            }
        }.mapError { throwable ->
            Timber.e(t = throwable, message = "Failed to update batch size for credentialId: $credentialId")
            CredentialRefreshDataError.Unexpected(throwable)
        }
    }

    private suspend fun batchCredentialRefreshDataDao(): VerifiableCredentialWithBatchDataAndAuthenticationDao = suspendUntilNonNull {
        verifiableCredentialWithBatchDataAndAuthenticationDaoFlow.value
    }

    private suspend fun credentialRefreshDataDao(): VerifiableCredentialWithAuthenticationDao = suspendUntilNonNull {
        verifiableCredentialWithAuthenticationDaoFlow.value
    }

    private suspend fun batchRefreshDataDao(): BatchRefreshDataDao = suspendUntilNonNull {
        batchRefreshDataDaoFlow.value
    }
    private suspend fun credentialAuthenticationDao(): CredentialAuthenticationDao =
        suspendUntilNonNull {
            credentialAuthenticationDaoFlow.value
        }
    private suspend fun dpopBindingDao(): DpopBindingDao = suspendUntilNonNull {
        dpopBindingDaoFlow.value
    }

    private val verifiableCredentialWithAuthenticationDaoFlow = daoProvider.verifiableCredentialWithAuthenticationDao
    private val verifiableCredentialWithBatchDataAndAuthenticationDaoFlow =
        daoProvider.verifiableCredentialWithBatchDataAndAuthenticationDaoFlow
    private val batchRefreshDataDaoFlow = daoProvider.batchRefreshDataDao
    private val credentialAuthenticationDaoFlow = daoProvider.credentialAuthenticationDaoFlow
    private val dpopBindingDaoFlow = daoProvider.dpopBindingDaoFlow
}
