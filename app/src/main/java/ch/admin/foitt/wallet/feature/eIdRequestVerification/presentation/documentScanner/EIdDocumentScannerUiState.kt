package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.documentScanner

import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.ScannerButtonState
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.model.SDKInfoState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.DocumentScannerErrorType

sealed interface EIdDocumentScannerUiState {
    data object Initializing : EIdDocumentScannerUiState
    data class Scan(
        val infoState: SDKInfoState,
        val infoText: Int?,
        val status: EIdDocumentScanStatus,
    ) : EIdDocumentScannerUiState

    data class Error(
        val type: DocumentScannerErrorType,
        val onRetry: () -> Unit,
        val onHelp: (() -> Unit)? = null,
    ) : EIdDocumentScannerUiState
}

enum class EIdDocumentScanStatus {
    FRONTSIDE,
    FRONTSIDE_SCANNING,
    BACKSIDE_INFO,
    BACKSIDE,
    BACKSIDE_SCANNING,
    FINISHED;

    fun toScannerButtonState(): ScannerButtonState {
        return when (this) {
            FRONTSIDE,
            BACKSIDE_INFO,
            BACKSIDE -> ScannerButtonState.Ready

            FRONTSIDE_SCANNING,
            BACKSIDE_SCANNING -> ScannerButtonState.Scanning

            FINISHED -> ScannerButtonState.Done
        }
    }

    val shouldShowOverlayBackside: Boolean
        get() = when (this) {
            FRONTSIDE,
            BACKSIDE_INFO,
            FRONTSIDE_SCANNING -> false

            BACKSIDE,
            BACKSIDE_SCANNING,
            FINISHED -> true
        }

    val isScanning: Boolean
        get() = this != FRONTSIDE && this != FINISHED
}
