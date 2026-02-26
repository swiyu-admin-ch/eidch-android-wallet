package ch.admin.foitt.wallet.platform.batch.domain.error

import ch.admin.foitt.wallet.platform.ssi.domain.model.BundleItemRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.BundleItemWithKeyBindingRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError

sealed interface DeleteBundleItemsByAmountError {
    class Unexpected(val cause: Throwable?) : DeleteBundleItemsByAmountError
}

fun BundleItemWithKeyBindingRepositoryError.toDeleteBundleItemsByAmountError() = when (this) {
    is SsiError.Unexpected -> DeleteBundleItemsByAmountError.Unexpected(cause)
}

fun BundleItemRepositoryError.toDeleteBundleItemsByAmountError() = when (this) {
    is SsiError.Unexpected -> DeleteBundleItemsByAmountError.Unexpected(cause)
}
