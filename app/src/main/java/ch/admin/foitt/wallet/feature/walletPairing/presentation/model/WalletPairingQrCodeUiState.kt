package ch.admin.foitt.wallet.feature.walletPairing.presentation.model

sealed interface WalletPairingQrCodeUiState {
    data object Polling : WalletPairingQrCodeUiState
    data object LoadingInvitation : WalletPairingQrCodeUiState
    data object Done : WalletPairingQrCodeUiState
    data object LoadingInvitationError : WalletPairingQrCodeUiState
    data object PairingTimeout : WalletPairingQrCodeUiState
    data object PairingFailure : WalletPairingQrCodeUiState
    data object NetworkError : WalletPairingQrCodeUiState
    data object UnexpectedError : WalletPairingQrCodeUiState
}
