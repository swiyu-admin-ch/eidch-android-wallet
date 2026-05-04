package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.documentRecording

import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.ScannerButtonState
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.model.SDKInfoState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.DocumentScannerErrorType

sealed interface EIdDocumentRecordingUiState {

    data object Initializing : EIdDocumentRecordingUiState

    data class Recording(
        val infoState: SDKInfoState,
        val infoText: Int?,
        val showSecondSide: Boolean,
        val scannerButtonState: ScannerButtonState,
    ) : EIdDocumentRecordingUiState

    data class Error(
        val type: DocumentScannerErrorType,
        val onRetry: () -> Unit,
        val onHelp: (() -> Unit)? = null,
    ) : EIdDocumentRecordingUiState
}
