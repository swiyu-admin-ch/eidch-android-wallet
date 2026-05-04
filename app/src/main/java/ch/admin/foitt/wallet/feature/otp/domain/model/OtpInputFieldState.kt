package ch.admin.foitt.wallet.feature.otp.domain.model

sealed interface OtpInputFieldState {
    data object Initial : OtpInputFieldState
    data object Edited : OtpInputFieldState
}
