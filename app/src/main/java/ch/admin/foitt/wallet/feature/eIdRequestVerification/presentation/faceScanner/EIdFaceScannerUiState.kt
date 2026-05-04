package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.faceScanner

import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.ScannerButtonState
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.model.SDKInfoState

sealed interface EIdFaceScannerUiState {
    data object Initializing : EIdFaceScannerUiState
    data class Scan(
        val infoState: SDKInfoState,
        val infoText: Int?,
        val isProcessing: Boolean = false,
        val scannerButtonState: ScannerButtonState,
    ) : EIdFaceScannerUiState
    data class Error(val onRetry: () -> Unit) : EIdFaceScannerUiState
    data object Finish : EIdFaceScannerUiState
}
