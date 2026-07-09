@file:Suppress("TooManyFunctions")

package ch.admin.foitt.wallet.feature.presentationRequest.domain.model

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.GetAuthorizationResponseConfigError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.SubmitAnyCredentialPresentationError
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.MapToCredentialDisplayDataError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ProximitySubmissionError
import ch.admin.foitt.wallet.platform.ssi.domain.model.BundleItemRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialWithDisplaysRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialWithKeyBindingRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.GetCredentialsWithDetailsFlowError
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.model.VerifiableCredentialRepositoryError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestError as OpenIdPresentationRequestError

interface PresentationRequestError {
    data object RawSdJwtParsingError : SubmitPresentationError
    data class ValidationError(val error: String, val description: String?) : SubmitPresentationError
    data object InvalidCredentialError : SubmitPresentationError
    data object VerificationError : SubmitPresentationError
    data object InvalidUrl : SubmitPresentationError
    data object NetworkError : SubmitPresentationError
    data object SocketTimeoutError : SubmitPresentationError
    data class Unexpected(val throwable: Throwable?) :
        SubmitPresentationError,
        GetPresentationRequestFlowError,
        GetPresentationRequestCredentialListFlowError
}

sealed interface SubmitPresentationError
sealed interface GetPresentationRequestFlowError
sealed interface GetPresentationRequestCredentialListFlowError

fun SubmitAnyCredentialPresentationError.toSubmitPresentationError(): SubmitPresentationError = when (this) {
    OpenIdPresentationRequestError.NetworkError -> PresentationRequestError.NetworkError
    OpenIdPresentationRequestError.SocketTimeoutError -> PresentationRequestError.SocketTimeoutError
    is OpenIdPresentationRequestError.ValidationError -> PresentationRequestError.ValidationError(error, description)
    OpenIdPresentationRequestError.VerificationError -> PresentationRequestError.VerificationError
    OpenIdPresentationRequestError.InvalidCredentialError -> PresentationRequestError.InvalidCredentialError
    is OpenIdPresentationRequestError.Unexpected -> PresentationRequestError.Unexpected(throwable)
}

fun ProximitySubmissionError.toSubmitPresentationError(): SubmitPresentationError = when (this) {
    is ProximitySubmissionError.Failed -> PresentationRequestError.Unexpected(this)
    ProximitySubmissionError.UnexpectedTermination -> PresentationRequestError.Unexpected(this)
}

fun CredentialWithDisplaysRepositoryError.toGetPresentationRequestFlowError():
    GetPresentationRequestFlowError = when (this) {
    is SsiError.Unexpected -> PresentationRequestError.Unexpected(cause)
}

fun GetCredentialsWithDetailsFlowError.toGetPresentationRequestCredentialListFlowError():
    GetPresentationRequestCredentialListFlowError = when (this) {
    is SsiError.Unexpected -> PresentationRequestError.Unexpected(cause)
}

fun MapToCredentialDisplayDataError.toGetPresentationRequestFlowError(): GetPresentationRequestFlowError = when (this) {
    is CredentialError.Unexpected -> PresentationRequestError.Unexpected(cause)
}

internal fun GetAuthorizationResponseConfigError.toSubmitPresentationError(): SubmitPresentationError =
    when (this) {
        is OpenIdPresentationRequestError.Unexpected -> PresentationRequestError.Unexpected(throwable)
    }

fun AnyCredentialError.toSubmitPresentationError(): SubmitPresentationError = when (this) {
    is CredentialError.Unexpected -> PresentationRequestError.Unexpected(cause)
}

fun BundleItemRepositoryError.toSubmitPresentationError(): SubmitPresentationError = when (this) {
    is SsiError.Unexpected -> PresentationRequestError.Unexpected(cause)
}

fun CredentialWithKeyBindingRepositoryError.toSubmitPresentationError(): SubmitPresentationError = when (this) {
    is SsiError.Unexpected -> PresentationRequestError.Unexpected(cause)
}

fun VerifiableCredentialRepositoryError.toSubmitPresentationError(): SubmitPresentationError = when (this) {
    is SsiError.Unexpected -> PresentationRequestError.Unexpected(cause)
}
