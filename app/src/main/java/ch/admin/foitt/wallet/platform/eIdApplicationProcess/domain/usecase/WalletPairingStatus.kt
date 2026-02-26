package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.WalletPairingStateError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.WalletPairingStateResponse
import com.github.michaelbull.result.Result

fun interface WalletPairingStatus {
    suspend operator fun invoke(caseId: String, walletPairingId: String): Result<WalletPairingStateResponse, WalletPairingStateError>
}
