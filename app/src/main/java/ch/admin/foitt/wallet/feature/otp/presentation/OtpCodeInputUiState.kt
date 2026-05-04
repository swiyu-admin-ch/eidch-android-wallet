package ch.admin.foitt.wallet.feature.otp.presentation

sealed interface OtpCodeInputUiState {
    data object Initial : OtpCodeInputUiState
    data object Loading : OtpCodeInputUiState
    data object Unexpected : OtpCodeInputUiState
    data object NetworkError : OtpCodeInputUiState
    data object Unavailable : OtpCodeInputUiState

    data object NotSupported : OtpCodeInputUiState
    data object TooManyAttempts : OtpCodeInputUiState
    data object WrongCode : OtpCodeInputUiState
}
