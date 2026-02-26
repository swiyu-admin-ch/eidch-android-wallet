package ch.admin.foitt.wallet.platform.ssi.data.repository

import ch.admin.foitt.wallet.platform.database.data.dao.BundleItemEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.DaoProvider
import ch.admin.foitt.wallet.platform.database.domain.model.BundleItemEntity
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.PresentableBatchItemCount
import ch.admin.foitt.wallet.platform.di.IoDispatcher
import ch.admin.foitt.wallet.platform.ssi.domain.model.BundleItemRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.BundleItemRepository
import ch.admin.foitt.wallet.platform.utils.suspendUntilNonNull
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class BundleItemRepositoryImpl @Inject constructor(
    daoProvider: DaoProvider,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : BundleItemRepository {
    override suspend fun deleteByIds(bundleItemIds: List<Long>): Result<Int, BundleItemRepositoryError> =
        runSuspendCatching {
            withContext(ioDispatcher) {
                bundleItemDao().deleteByIds(bundleItemIds)
            }
        }.mapError { throwable ->
            Timber.e(t = throwable, message = "Failed to get all verifiable credentials")
            SsiError.Unexpected(throwable)
        }

    override suspend fun getAllByCredentialId(credentialId: Long): Result<List<BundleItemEntity>, BundleItemRepositoryError> =
        runSuspendCatching {
            withContext(ioDispatcher) {
                bundleItemDao().getAllByCredentialId(credentialId)
            }
        }.mapError { throwable ->
            Timber.e(t = throwable, message = "Failed to get all verifiable credentials")
            SsiError.Unexpected(throwable)
        }

    override suspend fun getCountOfNeverPresented(): Result<List<PresentableBatchItemCount>, BundleItemRepositoryError> =
        runSuspendCatching {
            withContext(ioDispatcher) {
                bundleItemDao().getCountOfNeverPresented()
            }
        }.mapError { throwable ->
            Timber.e(t = throwable, message = "Failed to get all verifiable credentials")
            SsiError.Unexpected(throwable)
        }

    override suspend fun updateStatusByCredentialId(
        credentialId: Long,
        status: CredentialStatus,
    ): Result<Int, BundleItemRepositoryError> = runSuspendCatching {
        withContext(ioDispatcher) {
            bundleItemDao().updateStatusByCredentialId(
                id = credentialId,
                status = status,
            )
        }
    }.mapError { throwable ->
        Timber.e(throwable)
        SsiError.Unexpected(throwable)
    }

    private suspend fun bundleItemDao(): BundleItemEntityDao = suspendUntilNonNull {
        bundleItemDaoFlow.value
    }

    private val bundleItemDaoFlow = daoProvider.bundleItemEntityDaoFlow
}
