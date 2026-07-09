package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.documentRecording

import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.ScannerButtonState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.DocumentScannerErrorType

sealed interface EIdDocumentRecordingUiState {

    data object Initializing : EIdDocumentRecordingUiState

    data class Recording(
        val infoText: Int?,
        val status: EIdDocumentRecordingStatus,
    ) : EIdDocumentRecordingUiState

    data class Error(
        val type: DocumentScannerErrorType,
        val onRetry: () -> Unit,
        val onHelp: (() -> Unit)? = null,
    ) : EIdDocumentRecordingUiState
}

sealed interface EIdDocumentRecordingStatus {
    data object Initializing : EIdDocumentRecordingStatus
    data object FrontSide : EIdDocumentRecordingStatus
    data class FrontSideScanning(val progressRatio: Float) : EIdDocumentRecordingStatus
    data class BackSideScanning(val progressRatio: Float) : EIdDocumentRecordingStatus
    data object Finished : EIdDocumentRecordingStatus

    fun toScannerButtonState(): ScannerButtonState {
        return when (this) {
            Initializing -> ScannerButtonState.Initializing
            FrontSide -> ScannerButtonState.Ready
            is FrontSideScanning -> ScannerButtonState.Scanning(progressRatio)
            is BackSideScanning -> ScannerButtonState.Scanning(progressRatio)
            Finished -> ScannerButtonState.Done
        }
    }

    val shouldShowOverlayBackside: Boolean
        get() = when (this) {
            Initializing,
            FrontSide,
            is FrontSideScanning -> false

            is BackSideScanning,
            Finished -> true
        }

    val isRecording: Boolean
        get() = this !is FrontSide && this !is Finished
}
