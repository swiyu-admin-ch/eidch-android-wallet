package ch.admin.foitt.openid4vc.domain.model.vcSdJwt

import ch.admin.foitt.openid4vc.domain.model.ResolveDidError
import ch.admin.foitt.openid4vc.domain.model.jwt.JwtError
import ch.admin.foitt.openid4vc.domain.model.jwt.VerifyJwtSignatureError
import ch.admin.foitt.openid4vc.domain.model.jwt.VerifyJwtSignatureFromDidError

interface VcSdJwtError {
    data object IssuerValidationFailed :
        VerifyRequestObjectSignatureError,
        VerifyJwtError,
        VerifyVcSdJwtSignatureError,
        ResolvePublicKeyError

    data object InvalidJwt :
        VerifyRequestObjectSignatureError,
        VerifyJwtError,
        VerifyVcSdJwtSignatureError,
        ResolvePublicKeyError

    data object DidDocumentDeactivated :
        VerifyRequestObjectSignatureError,
        VerifyJwtError,
        VerifyVcSdJwtSignatureError,
        ResolvePublicKeyError

    data object NetworkError :
        VerifyRequestObjectSignatureError,
        VerifyJwtError,
        VerifyVcSdJwtSignatureError,
        ResolvePublicKeyError

    data object InvalidRequestObject : VerifyRequestObjectSignatureError

    data class InvalidVcSdJwt(val cause: Throwable) : VerifyVcSdJwtSignatureError

    data class Unexpected(val cause: Throwable?) :
        VerifyRequestObjectSignatureError,
        VerifyJwtError,
        VerifyVcSdJwtSignatureError,
        ResolvePublicKeyError
}

sealed interface VerifyRequestObjectSignatureError
sealed interface VerifyJwtError
sealed interface VerifyVcSdJwtSignatureError
sealed interface ResolvePublicKeyError

internal fun ResolveDidError.toVerifyRequestObjectSignatureError(): VerifyRequestObjectSignatureError = when (this) {
    is ResolveDidError.ValidationFailure -> VcSdJwtError.IssuerValidationFailed
    is ResolveDidError.NetworkError -> VcSdJwtError.NetworkError
    is ResolveDidError.Unexpected -> VcSdJwtError.Unexpected(cause)
}

internal fun VerifyJwtSignatureError.toVerifyRequestObjectSignatureError(): VerifyRequestObjectSignatureError = when (this) {
    is JwtError.InvalidJwt -> VcSdJwtError.InvalidJwt
    is JwtError.Unexpected -> VcSdJwtError.Unexpected(throwable)
}

internal fun VerifyJwtSignatureFromDidError.toVerifyRequestObjectSignatureError(): VerifyRequestObjectSignatureError = when (this) {
    is JwtError.InvalidJwt -> VcSdJwtError.InvalidJwt
    is JwtError.IssuerValidationFailed -> VcSdJwtError.IssuerValidationFailed
    is JwtError.DidDocumentDeactivated -> VcSdJwtError.DidDocumentDeactivated
    is JwtError.NetworkError -> VcSdJwtError.NetworkError
    is JwtError.Unexpected -> VcSdJwtError.Unexpected(throwable)
}

internal fun ResolveDidError.toResolvePublicKeyError(): ResolvePublicKeyError = when (this) {
    is ResolveDidError.ValidationFailure -> VcSdJwtError.IssuerValidationFailed
    is ResolveDidError.NetworkError -> VcSdJwtError.NetworkError
    is ResolveDidError.Unexpected -> VcSdJwtError.Unexpected(cause)
}

internal fun Throwable.toVerifyVcSdJwtSignatureError(): VerifyVcSdJwtSignatureError =
    VcSdJwtError.InvalidVcSdJwt(this)

internal fun VerifyJwtSignatureFromDidError.toVerifyVcSdJwtSignatureError(): VerifyVcSdJwtSignatureError = when (this) {
    is JwtError.InvalidJwt -> VcSdJwtError.InvalidJwt
    is JwtError.IssuerValidationFailed -> VcSdJwtError.IssuerValidationFailed
    is JwtError.DidDocumentDeactivated -> VcSdJwtError.DidDocumentDeactivated
    is JwtError.NetworkError -> VcSdJwtError.NetworkError
    is JwtError.Unexpected -> VcSdJwtError.Unexpected(throwable)
}
