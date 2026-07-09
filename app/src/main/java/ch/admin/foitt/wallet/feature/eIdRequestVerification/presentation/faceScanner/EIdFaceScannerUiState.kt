package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.faceScanner

import androidx.annotation.StringRes
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.ScannerButtonState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.DocumentScannerErrorType

sealed interface EIdFaceScannerUiState {
    data object Initializing : EIdFaceScannerUiState
    data class Scanning(
        val infoText: Int?,
        val status: EIdFaceScanStatus,
    ) : EIdFaceScannerUiState
    data class Error(
        val type: DocumentScannerErrorType,
        @param:StringRes val title: Int,
        @param:StringRes val content: Int,
        @param:StringRes val buttonText: Int,
        val onButton: () -> Unit,
        val onHelp: () -> Unit,
    ) : EIdFaceScannerUiState
}

sealed interface EIdFaceScanStatus {
    data object Initializing : EIdFaceScanStatus
    data object Ready : EIdFaceScanStatus
    data class Scanning(val progressRatio: Float) : EIdFaceScanStatus
    data object Finished : EIdFaceScanStatus

    fun toScannerButtonState(): ScannerButtonState {
        return when (this) {
            Initializing -> ScannerButtonState.Initializing
            Ready -> ScannerButtonState.Ready
            is Scanning -> ScannerButtonState.Scanning(progressRatio)
            Finished -> ScannerButtonState.Done
        }
    }

    val isScanning: Boolean get() = this is Scanning
}
