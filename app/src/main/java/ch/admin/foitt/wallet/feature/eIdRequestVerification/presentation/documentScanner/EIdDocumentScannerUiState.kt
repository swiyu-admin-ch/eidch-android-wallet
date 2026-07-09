package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.documentScanner

import androidx.annotation.StringRes
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.ScannerButtonState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.DocumentScannerErrorType

sealed interface EIdDocumentScannerUiState {
    data object Initializing : EIdDocumentScannerUiState
    data class Scan(
        val infoText: Int?,
        val status: EIdDocumentScanStatus,
    ) : EIdDocumentScannerUiState

    data class Error(
        val type: DocumentScannerErrorType,
        @param:StringRes val title: Int,
        @param:StringRes val content: Int,
        @param:StringRes val buttonText: Int,
        val onButton: () -> Unit,
        val onHelp: (() -> Unit)? = null,
    ) : EIdDocumentScannerUiState
}

enum class EIdDocumentScanStatus {
    INITIALIZING,
    FRONTSIDE,
    FRONTSIDE_SCANNING,
    BACKSIDE_INFO,
    BACKSIDE,
    BACKSIDE_SCANNING,
    FINISHED;

    fun toScannerButtonState(): ScannerButtonState {
        return when (this) {
            INITIALIZING,
            BACKSIDE_INFO -> ScannerButtonState.Initializing
            FRONTSIDE,
            BACKSIDE -> ScannerButtonState.Ready
            FRONTSIDE_SCANNING,
            BACKSIDE_SCANNING -> ScannerButtonState.Scanning(0f)

            FINISHED -> ScannerButtonState.Done
        }
    }

    val shouldShowOverlayBackside: Boolean
        get() = when (this) {
            INITIALIZING,
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
