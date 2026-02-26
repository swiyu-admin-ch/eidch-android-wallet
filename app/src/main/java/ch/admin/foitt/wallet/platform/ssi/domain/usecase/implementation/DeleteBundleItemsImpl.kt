package ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.ssi.domain.model.BundleItemRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.BundleItemWithKeyBindingRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.DeleteBundleItemError
import ch.admin.foitt.wallet.platform.ssi.domain.model.toDeleteBundleItemError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.BundleItemRepository
import ch.admin.foitt.wallet.platform.ssi.domain.repository.BundleItemWithKeyBindingRepository
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteBundleItems
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteKeyStoreEntry
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class DeleteBundleItemsImpl @Inject constructor(
    private val bundleItemWithKeyBindingRepository: BundleItemWithKeyBindingRepository,
    private val bundleItemRepository: BundleItemRepository,
    private val deleteKeyStoreEntry: DeleteKeyStoreEntry,
) : DeleteBundleItems {
    override suspend fun invoke(bundleItemIds: List<Long>): Result<Int, DeleteBundleItemError> = coroutineBinding {
        val bundleItemWithKeyBindings = bundleItemWithKeyBindingRepository.getByBundleItemIds(bundleItemIds)
            .mapError(BundleItemWithKeyBindingRepositoryError::toDeleteBundleItemError)
            .bind()

        bundleItemWithKeyBindings.forEach { bundleItemWithKeyBinding ->
            bundleItemWithKeyBinding.keyBinding?.let {
                deleteKeyStoreEntry(it.id)
            }
        }

        bundleItemRepository.deleteByIds(bundleItemIds)
            .mapError(BundleItemRepositoryError::toDeleteBundleItemError)
            .bind()
    }
}
