package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.documentScanner

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdDocumentType

sealed interface EIdDocumentScanSummaryUiState {
    object Initial : EIdDocumentScanSummaryUiState

    @Suppress("ArrayInDataClass")
    data class Ready(
        val documentType: EIdDocumentType,
        val frontsideImage: ByteArray,
        val backsideImage: ByteArray,
    ) : EIdDocumentScanSummaryUiState

    object Error : EIdDocumentScanSummaryUiState
}
