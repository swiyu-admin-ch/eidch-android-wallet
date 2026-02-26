package ch.admin.foitt.wallet.platform.batch.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.batch.domain.error.DeleteBundleItemsByAmountError
import ch.admin.foitt.wallet.platform.batch.domain.error.toDeleteBundleItemsByAmountError
import ch.admin.foitt.wallet.platform.batch.domain.usecase.DeleteBundleItemsByAmount
import ch.admin.foitt.wallet.platform.ssi.domain.model.BundleItemRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.BundleItemWithKeyBindingRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.BundleItemRepository
import ch.admin.foitt.wallet.platform.ssi.domain.repository.BundleItemWithKeyBindingRepository
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteKeyStoreEntry
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class DeleteBundleItemsByAmountImpl @Inject constructor(
    private val bundleItemRepository: BundleItemRepository,
    private val bundleItemWithKeyBindingRepository: BundleItemWithKeyBindingRepository,
    private val deleteKeyStoreEntry: DeleteKeyStoreEntry,
) : DeleteBundleItemsByAmount {
    override suspend fun invoke(credentialId: Long, amount: Int): Result<Unit, DeleteBundleItemsByAmountError> = coroutineBinding {
        val bundleItemsWithKeyBinding = bundleItemWithKeyBindingRepository.getBundleItemsWithKeyBindingsToDelete(credentialId, amount)
            .mapError(BundleItemWithKeyBindingRepositoryError::toDeleteBundleItemsByAmountError)
            .bind()
        bundleItemsWithKeyBinding.forEach { bundleItemWithKeyBinding ->
            bundleItemWithKeyBinding.keyBinding?.let {
                deleteKeyStoreEntry(it.id)
            }
        }
        bundleItemRepository.deleteByIds(bundleItemsWithKeyBinding.map { it.bundleItem.id })
            .mapError(BundleItemRepositoryError::toDeleteBundleItemsByAmountError)
            .bind()
    }
}
