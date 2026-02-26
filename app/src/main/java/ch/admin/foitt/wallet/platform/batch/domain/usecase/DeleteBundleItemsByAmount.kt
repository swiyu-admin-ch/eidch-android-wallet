package ch.admin.foitt.wallet.platform.batch.domain.usecase

import ch.admin.foitt.wallet.platform.batch.domain.error.DeleteBundleItemsByAmountError
import com.github.michaelbull.result.Result

interface DeleteBundleItemsByAmount {
    suspend operator fun invoke(credentialId: Long, amount: Int): Result<Unit, DeleteBundleItemsByAmountError>
}
