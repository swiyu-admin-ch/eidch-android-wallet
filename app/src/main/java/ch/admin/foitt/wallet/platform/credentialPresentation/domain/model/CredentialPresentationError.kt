@file:Suppress("TooManyFunctions")

package ch.admin.foitt.wallet.platform.credentialPresentation.domain.model

import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VerifyJwtError
import ch.admin.foitt.wallet.platform.batch.domain.error.RefreshBatchCredentialsError
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialWithKeyBindingRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.model.VerifiableCredentialRepositoryError
import ch.admin.foitt.wallet.platform.utils.JsonError
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import timber.log.Timber

internal interface CredentialPresentationError {
    data class EmptyWallet(val responseUri: String? = null) : ProcessPresentationRequestError
    data class NoCompatibleCredential(val responseUri: String? = null) : ProcessPresentationRequestError
    data class InvalidPresentation(val responseUri: String) :
        ProcessPresentationRequestError,
        ValidatePresentationRequestError
    data object UnknownVerifier : ValidatePresentationRequestError, ProcessPresentationRequestError
    data object NetworkError : ValidatePresentationRequestError, ProcessPresentationRequestError
    data class Unexpected(val cause: Throwable?) :
        ProcessPresentationRequestError,
        GetCompatibleCredentialsError,
        GetRequestedFieldsError,
        ValidatePresentationRequestError
}

sealed interface ProcessPresentationRequestError
sealed interface ValidatePresentationRequestError
sealed interface GetCompatibleCredentialsError
sealed interface GetRequestedFieldsError

internal fun VerifiableCredentialRepositoryError.toRefreshBatchCredentialsError(): RefreshBatchCredentialsError = when (this) {
    is SsiError.Unexpected -> RefreshBatchCredentialsError.Unexpected(cause)
}

internal fun ValidatePresentationRequestError.toProcessPresentationRequestError(): ProcessPresentationRequestError = when (this) {
    is CredentialPresentationError.InvalidPresentation -> this
    is CredentialPresentationError.Unexpected -> this
    is CredentialPresentationError.UnknownVerifier -> this
    is CredentialPresentationError.NetworkError -> this
}

internal fun VerifiableCredentialRepositoryError.toProcessPresentationRequestError(): ProcessPresentationRequestError = when (this) {
    is SsiError.Unexpected -> CredentialPresentationError.Unexpected(cause)
}

internal fun GetCompatibleCredentialsError.toProcessPresentationRequestError(): ProcessPresentationRequestError = when (this) {
    is CredentialPresentationError.Unexpected -> this
}

internal fun GetRequestedFieldsError.toGetCompatibleCredentialsError(): GetCompatibleCredentialsError = when (this) {
    is CredentialPresentationError.Unexpected -> this
}

internal fun CredentialWithKeyBindingRepositoryError.toGetCompatibleCredentialsError(): GetCompatibleCredentialsError = when (this) {
    is SsiError.Unexpected -> CredentialPresentationError.Unexpected(cause)
}

internal fun Throwable.toGetCompatibleCredentialsError(message: String): GetCompatibleCredentialsError {
    Timber.e(t = this, message = message)
    return CredentialPresentationError.Unexpected(this)
}

internal fun Throwable.toGetRequestedFieldsError(message: String): GetRequestedFieldsError {
    Timber.e(t = this, message = message)
    return CredentialPresentationError.Unexpected(this)
}

internal fun VerifyJwtError.toValidatePresentationRequestError(responseUri: String): ValidatePresentationRequestError = when (this) {
    VcSdJwtError.NetworkError -> CredentialPresentationError.NetworkError
    VcSdJwtError.InvalidJwt,
    VcSdJwtError.DidDocumentDeactivated,
    is VcSdJwtError.Unexpected -> CredentialPresentationError.InvalidPresentation(responseUri)
    VcSdJwtError.IssuerValidationFailed -> CredentialPresentationError.UnknownVerifier
}

internal fun Throwable.toValidatePresentationRequestError(responseUri: String, message: String): ValidatePresentationRequestError {
    Timber.e(t = this, message = message)
    return CredentialPresentationError.InvalidPresentation(responseUri)
}

internal fun JsonParsingError.toValidatePresentationRequestError(): ValidatePresentationRequestError = when (this) {
    is JsonError.Unexpected -> CredentialPresentationError.Unexpected(throwable)
}
