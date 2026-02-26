package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model

data class RequestStatusInput(
    val queueState: EIdRequestQueueState,
    val legalConsent: LegalRepresentativeConsent?
)
