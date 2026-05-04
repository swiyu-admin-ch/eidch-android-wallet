package ch.admin.foitt.wallet.feature.otp.domain.model

sealed interface OtpValidationState {
    data object Valid : OtpCodeLengthValidationState, OtpEmailValidationState
    data object TooShort : OtpCodeLengthValidationState
    data object Invalid : OtpEmailValidationState
}

sealed interface OtpCodeLengthValidationState
sealed interface OtpEmailValidationState
