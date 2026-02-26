package ch.admin.foitt.wallet.platform.ssi.domain.usecase

import ch.admin.foitt.wallet.platform.ssi.domain.model.DeleteBundleItemError
import com.github.michaelbull.result.Result

interface DeleteBundleItems {
    suspend operator fun invoke(bundleItemIds: List<Long>): Result<Int, DeleteBundleItemError>
}
