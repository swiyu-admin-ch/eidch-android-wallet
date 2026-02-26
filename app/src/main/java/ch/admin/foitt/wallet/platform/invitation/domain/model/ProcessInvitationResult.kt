package ch.admin.foitt.wallet.platform.invitation.domain.model

import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestWithRaw

sealed interface ProcessInvitationResult {
    data class CredentialOffer(
        val credentialId: Long,
    ) : ProcessInvitationResult
    data class DeferredCredential(
        val credentialId: Long,
    ) : ProcessInvitationResult
    data class PresentationRequest(
        val credential: CompatibleCredential,
        val request: PresentationRequestWithRaw,
        val shouldCheckTrustStatement: Boolean,
    ) : ProcessInvitationResult
    data class PresentationRequestCredentialList(
        val credentials: Set<CompatibleCredential>,
        val request: PresentationRequestWithRaw,
        val shouldCheckTrustStatement: Boolean,
    ) : ProcessInvitationResult
}
