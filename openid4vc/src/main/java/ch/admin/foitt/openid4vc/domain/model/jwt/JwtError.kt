package ch.admin.foitt.openid4vc.domain.model.jwt

import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.ResolvePublicKeyError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import timber.log.Timber

interface JwtError {
    data object InvalidJwt : VerifyJwtSignatureError, VerifyJwtSignatureFromDidError
    data object DidDocumentDeactivated : VerifyJwtSignatureFromDidError
    data object IssuerValidationFailed : VerifyJwtSignatureFromDidError
    data object NetworkError : VerifyJwtSignatureFromDidError
    data class Unexpected(val throwable: Throwable?) : VerifyJwtSignatureError, VerifyJwtSignatureFromDidError
}

sealed interface VerifyJwtSignatureError
sealed interface VerifyJwtSignatureFromDidError

internal fun Throwable.toVerifyJwtSignatureError(message: String): VerifyJwtSignatureError {
    Timber.w(t = this, message = message)
    return JwtError.Unexpected(throwable = this)
}

internal fun ResolvePublicKeyError.toVerifyJwtSignatureFromDidError(): VerifyJwtSignatureFromDidError = when (this) {
    is VcSdJwtError.InvalidJwt -> JwtError.InvalidJwt
    is VcSdJwtError.NetworkError -> JwtError.NetworkError
    is VcSdJwtError.DidDocumentDeactivated -> JwtError.DidDocumentDeactivated
    is VcSdJwtError.IssuerValidationFailed -> JwtError.IssuerValidationFailed
    is VcSdJwtError.Unexpected -> JwtError.Unexpected(cause)
}

internal fun VerifyJwtSignatureError.toVerifyJwtSignatureFromDidError(): VerifyJwtSignatureFromDidError = when (this) {
    is JwtError.InvalidJwt -> this
    is JwtError.Unexpected -> this
}
