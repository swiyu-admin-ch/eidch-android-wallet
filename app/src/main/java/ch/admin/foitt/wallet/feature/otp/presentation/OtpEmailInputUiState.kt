package ch.admin.foitt.wallet.feature.otp.presentation

sealed interface OtpEmailInputUiState {
    data object Initial : OtpEmailInputUiState
    data object Unavailable : OtpEmailInputUiState
    data object ForbiddenEmail : OtpEmailInputUiState
    data object Unexpected : OtpEmailInputUiState
    data object NetworkError : OtpEmailInputUiState
    data object NotSupported : OtpEmailInputUiState
    data object Loading : OtpEmailInputUiState
}
