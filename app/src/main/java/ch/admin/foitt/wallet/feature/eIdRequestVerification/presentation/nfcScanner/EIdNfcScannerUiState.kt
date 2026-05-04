package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.nfcScanner

sealed interface EIdNfcScannerUiState {
    data object Initializing : EIdNfcScannerUiState
    data object Info : EIdNfcScannerUiState
    data object Scanning : EIdNfcScannerUiState
    data object ReadingChipData : EIdNfcScannerUiState
    data object Error : EIdNfcScannerUiState
    data object Failure : EIdNfcScannerUiState
    data object Success : EIdNfcScannerUiState
    data object NfcDisabled : EIdNfcScannerUiState
}
