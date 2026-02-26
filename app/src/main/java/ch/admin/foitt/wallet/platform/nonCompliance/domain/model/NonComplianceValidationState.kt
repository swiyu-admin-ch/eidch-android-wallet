package ch.admin.foitt.wallet.platform.nonCompliance.domain.model

sealed interface NonComplianceValidationState {
    data object Valid : NonComplianceTextLengthValidationState, NonComplianceEmailValidationState
    data object TooShort : NonComplianceTextLengthValidationState
    data object TooLong : NonComplianceTextLengthValidationState
    data object Invalid : NonComplianceEmailValidationState
}

sealed interface NonComplianceTextLengthValidationState
sealed interface NonComplianceEmailValidationState
