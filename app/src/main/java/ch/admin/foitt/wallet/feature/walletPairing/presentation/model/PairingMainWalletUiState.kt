package ch.admin.foitt.wallet.feature.walletPairing.presentation.model

internal sealed interface PairingMainWalletUiState {
    data object Initial : PairingMainWalletUiState
    data object SyncMainWallet : PairingMainWalletUiState
    data object MainWalletAdded : PairingMainWalletUiState
}
