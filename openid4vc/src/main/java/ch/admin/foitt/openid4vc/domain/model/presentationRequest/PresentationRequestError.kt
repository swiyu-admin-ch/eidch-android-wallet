@file:Suppress("TooManyFunctions")

package ch.admin.foitt.openid4vc.domain.model.presentationRequest

import ch.admin.foitt.openid4vc.domain.model.GetKeyPairError
import ch.admin.foitt.openid4vc.domain.model.GetSoftwareKeyPairError
import ch.admin.foitt.openid4vc.domain.model.KeyPairError
import ch.admin.foitt.openid4vc.domain.model.jwe.CreateJWEError
import ch.admin.foitt.openid4vc.domain.model.jwe.JWEError
import ch.admin.foitt.openid4vc.utils.JsonError
import ch.admin.foitt.openid4vc.utils.JsonParsingError
import timber.log.Timber

interface PresentationRequestError {
    data object NetworkError : FetchPresentationRequestError, SubmitAnyCredentialPresentationError, SubmitPresentationErrorError
    data object ValidationError : SubmitAnyCredentialPresentationError
    data object VerificationError : SubmitAnyCredentialPresentationError
    data object InvalidCredentialError : SubmitAnyCredentialPresentationError
    data object InvalidKeyPairError : CreateVcSdJwtVerifiablePresentationError
    data class Unexpected(val throwable: Throwable?) :
        FetchPresentationRequestError,
        SubmitAnyCredentialPresentationError,
        CreateAnyVerifiablePresentationError,
        CreateVcSdJwtVerifiablePresentationError,
        CreateAnyDescriptorMapsError,
        CreateVcSdJwtDescriptorMapError,
        SubmitPresentationErrorError,
        GetPresentationRequestTypeError
}

sealed interface FetchPresentationRequestError : PresentationRequestError
sealed interface SubmitAnyCredentialPresentationError : PresentationRequestError
internal sealed interface CreateAnyVerifiablePresentationError : PresentationRequestError
internal sealed interface CreateVcSdJwtVerifiablePresentationError : PresentationRequestError
internal sealed interface CreateAnyDescriptorMapsError : PresentationRequestError
internal sealed interface CreateVcSdJwtDescriptorMapError : PresentationRequestError
sealed interface SubmitPresentationErrorError : PresentationRequestError

sealed interface GetPresentationRequestTypeError : PresentationRequestError

internal fun CreateVcSdJwtVerifiablePresentationError.toCreateAnyVerifiablePresentationError(): CreateAnyVerifiablePresentationError =
    when (this) {
        is PresentationRequestError.InvalidKeyPairError -> PresentationRequestError.Unexpected(null)
        is PresentationRequestError.Unexpected -> this
    }

internal fun GetKeyPairError.toCreateVcSdJwtVerifiablePresentationError(): CreateVcSdJwtVerifiablePresentationError = when (this) {
    is KeyPairError.Unexpected -> PresentationRequestError.Unexpected(throwable)
    KeyPairError.NotFound -> PresentationRequestError.Unexpected(null)
}

internal fun GetSoftwareKeyPairError.toCreateVcSdJwtVerifiablePresentationError(): CreateVcSdJwtVerifiablePresentationError = when (this) {
    is KeyPairError.Unexpected -> PresentationRequestError.Unexpected(throwable)
}

internal fun CreateAnyVerifiablePresentationError.toSubmitAnyCredentialPresentationError(): SubmitAnyCredentialPresentationError =
    when (this) {
        is PresentationRequestError.Unexpected -> this
    }

internal fun Throwable.toSubmitAnyCredentialPresentationError(message: String): SubmitAnyCredentialPresentationError {
    Timber.e(t = this, message = message)
    return PresentationRequestError.Unexpected(this)
}

internal fun Throwable.toCreateVcSdJwtVerifiablePresentationError(message: String): CreateVcSdJwtVerifiablePresentationError {
    Timber.e(t = this, message = message)
    return PresentationRequestError.Unexpected(this)
}

internal fun JsonParsingError.toCreateVcSdJwtVerifiablePresentationError(): CreateVcSdJwtVerifiablePresentationError = when (this) {
    is JsonError.Unexpected -> PresentationRequestError.Unexpected(throwable)
}

internal fun JsonParsingError.toFetchPresentationRequestError(): FetchPresentationRequestError = when (this) {
    is JsonError.Unexpected -> PresentationRequestError.Unexpected(throwable)
}

internal fun JsonParsingError.toGetPresentationRequestTypeError(): GetPresentationRequestTypeError = when (this) {
    is JsonError.Unexpected -> PresentationRequestError.Unexpected(throwable)
}

internal fun CreateJWEError.toGetPresentationRequestTypeError(): GetPresentationRequestTypeError = when (this) {
    is JWEError.Unexpected -> PresentationRequestError.Unexpected(throwable)
}

internal fun GetPresentationRequestTypeError.toSubmitAnyCredentialPresentationError(): SubmitAnyCredentialPresentationError = when (this) {
    is PresentationRequestError.Unexpected -> this
}
