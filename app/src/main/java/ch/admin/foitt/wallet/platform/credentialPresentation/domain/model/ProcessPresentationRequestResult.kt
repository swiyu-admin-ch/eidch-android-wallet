package ch.admin.foitt.wallet.platform.credentialPresentation.domain.model

import ch.admin.foitt.wallet.platform.invitation.domain.model.ProcessInvitationResult

sealed interface ProcessPresentationRequestResult {
    data class Credential(
        val credential: CompatibleCredential,
        val presentationRequest: PresentationRequestWithRaw,
    ) : ProcessPresentationRequestResult

    data class CredentialList(
        val credentials: Set<CompatibleCredential>,
        val presentationRequest: PresentationRequestWithRaw,
    ) : ProcessPresentationRequestResult

    fun toProcessInvitationResult(): ProcessInvitationResult =
        when (this) {
            is Credential -> ProcessInvitationResult.PresentationRequest(
                credential = credential,
                request = presentationRequest,
            )

            is CredentialList ->
                ProcessInvitationResult.PresentationRequestCredentialList(
                    credentials = credentials,
                    request = presentationRequest,
                )
        }
}
