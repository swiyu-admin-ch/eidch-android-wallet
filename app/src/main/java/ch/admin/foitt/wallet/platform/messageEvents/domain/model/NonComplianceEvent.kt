package ch.admin.foitt.wallet.platform.messageEvents.domain.model

sealed class NonComplianceEvent {
    data object None : NonComplianceEvent()
    data object ReportSent : NonComplianceEvent()
    data object ReportSentHome : NonComplianceEvent()
}
