package ch.admin.foitt.wallet.platform.credentialPresentation.domain.model

sealed interface ProcessPresentationRequestResult {
    data class Credential(
        val credential: CompatibleCredential,
        val presentationRequest: PresentationRequestWithRaw,
    ) : ProcessPresentationRequestResult

    data class CredentialList(
        val credentials: Set<CompatibleCredential>,
        val presentationRequest: PresentationRequestWithRaw,
    ) : ProcessPresentationRequestResult
}
