package ch.admin.foitt.wallet.platform.credentialPresentation.domain.model

import ch.admin.foitt.wallet.platform.utils.JsonError
import ch.admin.foitt.wallet.platform.utils.JsonParsingError

sealed class ProximitySubmissionError : Throwable() {
    data class Failed(val underlyingErrorMessage: String? = null) : ProximitySubmissionError()
    data object UnexpectedTermination : ProximitySubmissionError()
}

fun JsonParsingError.toProximitySubmissionError(): ProximitySubmissionError = when (this) {
    is JsonError.Unexpected -> ProximitySubmissionError.Failed(underlyingErrorMessage = throwable.message)
}
