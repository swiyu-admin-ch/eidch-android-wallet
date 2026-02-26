package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.PairCurrentWalletError
import com.github.michaelbull.result.Result

fun interface PairCurrentWallet {
    suspend operator fun invoke(
        caseId: String,
    ): Result<Unit, PairCurrentWalletError>
}
