package ch.admin.foitt.wallet.platform.passphraseInput.domain.model

sealed class PassphraseInputFieldState {
    data object Typing : PassphraseInputFieldState()
    data object Success : PassphraseInputFieldState()
    data class Error(val errorCode: String) : PassphraseInputFieldState()
}
