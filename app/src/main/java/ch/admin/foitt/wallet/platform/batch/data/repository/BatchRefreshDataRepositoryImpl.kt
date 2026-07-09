package ch.admin.foitt.wallet.platform.batch.data.repository

import ch.admin.foitt.openid4vc.domain.model.BatchSize
import ch.admin.foitt.openid4vc.domain.model.TokenType
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import ch.admin.foitt.wallet.platform.batch.domain.error.BatchRefreshDataRepositoryError
import ch.admin.foitt.wallet.platform.batch.domain.repository.BatchRefreshDataRepository
import ch.admin.foitt.wallet.platform.database.data.dao.BatchRefreshDataDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialAuthenticationDao
import ch.admin.foitt.wallet.platform.database.data.dao.DaoProvider
import ch.admin.foitt.wallet.platform.database.data.dao.DpopBindingDao
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

class BatchRefreshDataRepositoryImpl @Inject constructor(
    daoProvider: DaoProvider,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val runInTransaction: RunInTransaction,
) : BatchRefreshDataRepository {

    override suspend fun saveBatchRefreshData(
        credentialId: Long,
        batchSize: BatchSize,
        accessToken: String,
        refreshToken: String,
        dpopKeyBinding: KeyBinding?,
    ): Result<Long, BatchRefreshDataRepositoryError> = runSuspendCatching {
        withContext(ioDispatcher) {
            runInTransaction {
                batchRefreshDataDao().insert(
                    BatchRefreshDataEntity(
                        credentialId = credentialId,
                        batchSize = batchSize,
                    )
                )
                val existingAuthentication = credentialAuthenticationDao().getByCredentialId(credentialId)
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
            } ?: error("saveBatchRefreshData: transaction failed")
        }
    }.mapError { throwable ->
        Timber.e(t = throwable, message = "Failed to save batch refresh data")
        BatchRefreshDataRepositoryError.Unexpected(throwable)
    }

    override suspend fun updateBatchSize(credentialId: Long, batchSize: BatchSize): Result<Int, BatchRefreshDataRepositoryError> {
        return runSuspendCatching {
            withContext(ioDispatcher) {
                batchRefreshDataDao().updateBatchSize(credentialId, batchSize)
            }
        }.mapError { throwable ->
            Timber.e(t = throwable, message = "Failed to update batch size for credentialId: $credentialId")
            BatchRefreshDataRepositoryError.Unexpected(throwable)
        }
    }

    private suspend fun batchRefreshDataDao(): BatchRefreshDataDao = suspendUntilNonNull {
        batchRefreshDataDaoFlow.value
    }
    private suspend fun credentialAuthenticationDao(): CredentialAuthenticationDao = suspendUntilNonNull {
        credentialAuthenticationDaoFlow.value
    }
    private suspend fun dpopBindingDao(): DpopBindingDao = suspendUntilNonNull {
        dpopBindingDaoFlow.value
    }

    private val batchRefreshDataDaoFlow = daoProvider.batchRefreshDataDao
    private val credentialAuthenticationDaoFlow = daoProvider.credentialAuthenticationDaoFlow
    private val dpopBindingDaoFlow = daoProvider.dpopBindingDaoFlow
}
