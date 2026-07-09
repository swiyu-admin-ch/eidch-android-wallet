package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model

import ch.admin.foitt.avwrapper.AVBeamError
import ch.admin.foitt.avwrapper.AVBeamErrorType

sealed interface DocumentScannerErrorType {
    object None : DocumentScannerErrorType
    object Generic : DocumentScannerErrorType
    object UnequalDocuments : DocumentScannerErrorType
    data class SdkError(val errorCode: AVBeamError, val errorType: AVBeamErrorType) : DocumentScannerErrorType
}
