package ch.admin.foitt.wallet.platform.ssi.data.repository

import ch.admin.foitt.wallet.platform.database.data.dao.BundleItemWithKeyBindingDao
import ch.admin.foitt.wallet.platform.database.data.dao.DaoProvider
import ch.admin.foitt.wallet.platform.database.domain.model.BundleItemWithKeyBinding
import ch.admin.foitt.wallet.platform.di.IoDispatcher
import ch.admin.foitt.wallet.platform.ssi.domain.model.BundleItemWithKeyBindingRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.BundleItemWithKeyBindingRepository
import ch.admin.foitt.wallet.platform.utils.suspendUntilNonNull
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class BundleItemWithKeyBindingRepositoryImpl @Inject constructor(
    daoProvider: DaoProvider,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : BundleItemWithKeyBindingRepository {

    override suspend fun getAll():
        Result<List<BundleItemWithKeyBinding>, BundleItemWithKeyBindingRepositoryError> = runSuspendCatching {
        withContext(ioDispatcher) {
            dao().getAll()
        }
    }.mapError { throwable ->
        Timber.e(throwable)
        SsiError.Unexpected(throwable)
    }

    override suspend fun getByBundleItemIds(
        bundleItemIds: List<Long>
    ): Result<List<BundleItemWithKeyBinding>, BundleItemWithKeyBindingRepositoryError> = runSuspendCatching {
        withContext(ioDispatcher) {
            dao().getBundleItemWithKeyBindingByIds(bundleItemIds)
        }
    }.mapError { throwable ->
        Timber.e(throwable)
        SsiError.Unexpected(throwable)
    }

    override suspend fun getBundleItemsWithKeyBindingsToDelete(credentialId: Long, amount: Int):
        Result<List<BundleItemWithKeyBinding>, SsiError.Unexpected> = runSuspendCatching {
        withContext(ioDispatcher) {
            dao().getBundleItemsWithKeyBindingsToDelete(credentialId, amount)
        }
    }.mapError { throwable ->
        Timber.e(throwable)
        SsiError.Unexpected(throwable)
    }

    private suspend fun dao(): BundleItemWithKeyBindingDao = suspendUntilNonNull { daoFlow.value }
    private val daoFlow = daoProvider.bundleItemWithKeyBindingDaoFlow
}
