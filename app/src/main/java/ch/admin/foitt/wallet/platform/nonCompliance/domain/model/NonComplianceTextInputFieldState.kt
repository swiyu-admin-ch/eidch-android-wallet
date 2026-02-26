package ch.admin.foitt.wallet.platform.nonCompliance.domain.model

sealed class NonComplianceTextInputFieldState {
    data object Initial : NonComplianceTextInputFieldState()
    data object Success : NonComplianceTextInputFieldState()
    data object Error : NonComplianceTextInputFieldState()
}
