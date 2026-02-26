package ch.admin.foitt.wallet.feature.walletPairing.presentation.model

internal sealed interface WalletPairingUiState {
    data object Initial : WalletPairingUiState
    data object Unexpected : WalletPairingUiState
    data object NetworkError : WalletPairingUiState
}
