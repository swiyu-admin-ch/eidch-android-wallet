package ch.admin.foitt.wallet.platform.batch.data.repository

import ch.admin.foitt.openid4vc.domain.model.BatchSize
import ch.admin.foitt.wallet.platform.batch.domain.error.BatchRefreshDataRepositoryError
import ch.admin.foitt.wallet.platform.batch.domain.repository.BatchRefreshDataRepository
import ch.admin.foitt.wallet.platform.database.data.dao.BatchRefreshDataDao
import ch.admin.foitt.wallet.platform.database.data.dao.DaoProvider
import ch.admin.foitt.wallet.platform.database.domain.model.BatchRefreshDataEntity
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
) : BatchRefreshDataRepository {

    override suspend fun saveBatchRefreshData(
        credentialId: Long,
        batchSize: BatchSize,
        refreshToken: String
    ): Result<Long, BatchRefreshDataRepositoryError> = runSuspendCatching {
        withContext(ioDispatcher) {
            batchRefreshDataDao().insert(
                BatchRefreshDataEntity(
                    credentialId = credentialId,
                    batchSize = batchSize,
                    refreshToken = refreshToken
                )
            )
        }
    }.mapError { throwable ->
        Timber.e(t = throwable, message = "Failed to save batch refresh data")
        BatchRefreshDataRepositoryError.Unexpected(throwable)
    }

    override suspend fun getAll(): Result<List<BatchRefreshDataEntity>, BatchRefreshDataRepositoryError> = runSuspendCatching {
        withContext(ioDispatcher) {
            batchRefreshDataDao().getAll()
        }
    }.mapError { throwable ->
        Timber.e(t = throwable, message = "Failed to load all batch refresh data")
        BatchRefreshDataRepositoryError.Unexpected(throwable)
    }

    private suspend fun batchRefreshDataDao(): BatchRefreshDataDao = suspendUntilNonNull {
        batchRefreshDataDaoFlow.value
    }

    private val batchRefreshDataDaoFlow = daoProvider.batchRefreshDataDao
}
