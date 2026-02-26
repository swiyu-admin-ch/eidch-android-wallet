package ch.admin.foitt.wallet.feature.walletPairing.presentation.model

internal sealed interface PairingOtherWalletUiState {
    data object Open : PairingOtherWalletUiState
    data object LimitReached : PairingOtherWalletUiState
}
