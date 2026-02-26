package ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestFileRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestStateRepositoryError
import ch.admin.foitt.wallet.platform.utils.JsonError
import ch.admin.foitt.wallet.platform.utils.JsonParsingError

interface EIdRequestVerificationError {
    data class Unexpected(val cause: Throwable?) :
        SaveEIdRequestCaseError,
        GetEIdRequestCaseError,
        SaveEIdRequestStateError,
        GetImportantMrzKeysError,
        SaveEIdRequestFileError,
        GetDocumentScanDataError,
        AreEIdDocumentsEqualError
}

sealed interface SaveEIdRequestCaseError
sealed interface GetEIdRequestCaseError
sealed interface SaveEIdRequestStateError
sealed interface GetImportantMrzKeysError
sealed interface SaveEIdRequestFileError
sealed interface GetDocumentScanDataError
sealed interface AreEIdDocumentsEqualError

internal fun EIdRequestCaseRepositoryError.toSaveEIdRequestCaseError(): SaveEIdRequestCaseError = when (this) {
    is EIdRequestError.Unexpected -> EIdRequestVerificationError.Unexpected(cause)
}

internal fun EIdRequestCaseRepositoryError.toGetEIdRequestCaseError(): GetEIdRequestCaseError = when (this) {
    is EIdRequestError.Unexpected -> EIdRequestVerificationError.Unexpected(cause)
}

internal fun EIdRequestStateRepositoryError.toSaveEIdRequestStateError(): SaveEIdRequestStateError = when (this) {
    is EIdRequestError.Unexpected -> EIdRequestVerificationError.Unexpected(cause)
}

internal fun EIdRequestFileRepositoryError.toSaveEIdRequestFileError(): SaveEIdRequestFileError = when (this) {
    is EIdRequestError.Unexpected -> EIdRequestVerificationError.Unexpected(cause)
}

internal fun EIdRequestFileRepositoryError.toGetDocumentScanDataError(): GetDocumentScanDataError = when (this) {
    is EIdRequestError.Unexpected -> EIdRequestVerificationError.Unexpected(cause)
}

internal fun GetDocumentScanDataError.toAreEIdDocumentsEqualError(): AreEIdDocumentsEqualError = when (this) {
    is EIdRequestVerificationError.Unexpected -> EIdRequestVerificationError.Unexpected(cause)
}

internal fun JsonParsingError.toGetDocumentScanDataError(): GetDocumentScanDataError = when (this) {
    is JsonError.Unexpected -> EIdRequestVerificationError.Unexpected(throwable)
}
