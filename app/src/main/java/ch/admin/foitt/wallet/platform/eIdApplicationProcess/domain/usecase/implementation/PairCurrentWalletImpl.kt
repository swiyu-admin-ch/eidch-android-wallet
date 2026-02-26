package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.PairCurrentWalletError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.PairWalletError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.toPairCurrentWalletError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestCaseRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.PairCurrentWallet
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.PairWallet
import ch.admin.foitt.wallet.platform.invitation.domain.model.ProcessInvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.model.ProcessInvitationResult
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.ProcessInvitation
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class PairCurrentWalletImpl @Inject constructor(
    private val pairWallet: PairWallet,
    private val processInvitation: ProcessInvitation,
    private val eIdRequestCaseRepository: EIdRequestCaseRepository,
) : PairCurrentWallet {
    override suspend operator fun invoke(
        caseId: String,
    ): Result<Unit, PairCurrentWalletError> = coroutineBinding {
        val pairingResult = pairWallet(caseId = caseId)
            .mapError(PairWalletError::toPairCurrentWalletError).bind()
        val processInvitationResult = processInvitation(pairingResult.credentialOfferLink)
            .mapError(ProcessInvitationError::toPairCurrentWalletError).bind()

        when (processInvitationResult) {
            is ProcessInvitationResult.DeferredCredential -> {
                eIdRequestCaseRepository.setEIdRequestCaseCredentialId(
                    caseId = caseId,
                    credentialId = processInvitationResult.credentialId,
                ).mapError(EIdRequestCaseRepositoryError::toPairCurrentWalletError).bind()
            }
            is ProcessInvitationResult.CredentialOffer,
            is ProcessInvitationResult.PresentationRequest,
            is ProcessInvitationResult.PresentationRequestCredentialList -> {
                Err(EIdRequestError.InvalidDeferredCredentialOffer).bind()
            }
        }
    }
}
