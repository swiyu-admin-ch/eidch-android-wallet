@file:Suppress("TooManyFunctions")

package ch.admin.foitt.wallet.platform.credentialStatus.domain.model

import ch.admin.foitt.openid4vc.domain.model.jwt.JwtError
import ch.admin.foitt.openid4vc.domain.model.jwt.VerifyJwtSignatureFromDidError
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.GetAllAnyCredentialsByCredentialIdError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialStatusError.DidDocumentDeactivated
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialStatusError.NetworkError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialStatusError.Unexpected
import ch.admin.foitt.wallet.platform.ssi.domain.model.BundleItemRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.model.VerifiableCredentialRepositoryError
import ch.admin.foitt.wallet.platform.utils.JsonError
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import timber.log.Timber

interface CredentialStatusError {
    data object NetworkError :
        UpdateCredentialStatusError,
        FetchCredentialStatusError,
        FetchStatusFromTokenStatusListError,
        ValidateTokenStatusStatusListError
    data class Unexpected(val cause: Throwable?) :
        UpdateCredentialStatusError,
        FetchCredentialStatusError,
        FetchStatusFromTokenStatusListError,
        ValidateTokenStatusStatusListError,
        ParseTokenStatusStatusListError
    data object DidDocumentDeactivated :
        ValidateTokenStatusStatusListError
}

sealed interface UpdateCredentialStatusError
sealed interface FetchCredentialStatusError
sealed interface FetchStatusFromTokenStatusListError
sealed interface ValidateTokenStatusStatusListError
sealed interface ParseTokenStatusStatusListError

internal fun FetchCredentialStatusError.toUpdateCredentialStatusError(): UpdateCredentialStatusError = when (this) {
    is NetworkError -> this
    is Unexpected -> this
}

internal fun VerifiableCredentialRepositoryError.toUpdateCredentialStatusError(): UpdateCredentialStatusError = when (this) {
    is SsiError.Unexpected -> Unexpected(cause)
}

internal fun BundleItemRepositoryError.toUpdateCredentialStatusError(): UpdateCredentialStatusError = when (this) {
    is SsiError.Unexpected -> Unexpected(cause)
}

internal fun GetAllAnyCredentialsByCredentialIdError.toUpdateCredentialStatusError(): UpdateCredentialStatusError = when (this) {
    is CredentialError.Unexpected -> Unexpected(cause)
}

internal fun FetchStatusFromTokenStatusListError.toFetchCredentialStatusError(): FetchCredentialStatusError = when (this) {
    is NetworkError -> this
    is Unexpected -> this
}

internal fun ParseTokenStatusStatusListError.toFetchStatusFromParseTokenStatusListError(): FetchStatusFromTokenStatusListError =
    when (this) {
        is Unexpected -> this
    }

internal fun ValidateTokenStatusStatusListError.toFetchStatusFromTokenStatusListError(): FetchStatusFromTokenStatusListError = when (this) {
    is NetworkError -> this
    is Unexpected -> this
    DidDocumentDeactivated -> Unexpected(null)
}

internal fun JsonParsingError.toValidateTokenStatusListError(): ValidateTokenStatusStatusListError = when (this) {
    is JsonError.Unexpected -> Unexpected(throwable)
}

fun VerifyJwtSignatureFromDidError.toValidateTokenStatusListError(): ValidateTokenStatusStatusListError = when (this) {
    JwtError.DidDocumentDeactivated -> DidDocumentDeactivated
    JwtError.NetworkError -> NetworkError
    is JwtError.InvalidJwt,
    JwtError.IssuerValidationFailed -> Unexpected(null)
    is JwtError.Unexpected -> Unexpected(throwable)
}

internal fun Throwable.toValidateTokenStatusStatusListError(message: String): ValidateTokenStatusStatusListError {
    Timber.e(t = this, message = message)
    return Unexpected(this)
}

internal fun Throwable.toParseTokenStatusStatusListError(message: String): ParseTokenStatusStatusListError {
    Timber.e(t = this, message = message)
    return Unexpected(this)
}

internal fun Throwable.toUpdateCredentialStatusError(message: String): UpdateCredentialStatusError {
    Timber.e(t = this, message = message)
    return Unexpected(this)
}
