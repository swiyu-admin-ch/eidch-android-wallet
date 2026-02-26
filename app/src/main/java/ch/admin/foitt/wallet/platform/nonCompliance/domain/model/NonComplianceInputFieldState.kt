package ch.admin.foitt.wallet.platform.nonCompliance.domain.model

sealed class NonComplianceInputFieldState {
    data object Initial : NonComplianceInputFieldState()
    data object Edited : NonComplianceInputFieldState()
}
