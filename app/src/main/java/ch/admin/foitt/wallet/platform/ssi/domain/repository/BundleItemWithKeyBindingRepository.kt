package ch.admin.foitt.wallet.platform.ssi.domain.repository

import ch.admin.foitt.wallet.platform.database.domain.model.BundleItemWithKeyBinding
import ch.admin.foitt.wallet.platform.ssi.domain.model.BundleItemWithKeyBindingRepositoryError
import com.github.michaelbull.result.Result

interface BundleItemWithKeyBindingRepository {
    suspend fun getAll(): Result<List<BundleItemWithKeyBinding>, BundleItemWithKeyBindingRepositoryError>
    suspend fun getByBundleItemIds(
        bundleItemIds: List<Long>
    ): Result<List<BundleItemWithKeyBinding>, BundleItemWithKeyBindingRepositoryError>

    suspend fun getBundleItemsWithKeyBindingsToDelete(
        credentialId: Long,
        amount: Int
    ): Result<List<BundleItemWithKeyBinding>, BundleItemWithKeyBindingRepositoryError>
}
