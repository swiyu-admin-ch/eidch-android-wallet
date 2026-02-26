package ch.admin.foitt.openid4vc.domain.model.vcSdJwt

import ch.admin.foitt.openid4vc.domain.model.ResolveDidError

interface VcSdJwtError {
    data object IssuerValidationFailed : VerifyJwtError, VerifyVcSdJwtSignatureError
    data object InvalidJwt : VerifyJwtError, VerifyVcSdJwtSignatureError
    data object DidDocumentDeactivated : VerifyJwtError, VerifyVcSdJwtSignatureError
    data object NetworkError : VerifyJwtError, VerifyVcSdJwtSignatureError

    data class InvalidVcSdJwt(val cause: Throwable) : VerifyVcSdJwtSignatureError
    data class Unexpected(val cause: Throwable?) :
        VerifyJwtError,
        VerifyVcSdJwtSignatureError
}

sealed interface VerifyJwtError
sealed interface VerifyVcSdJwtSignatureError

internal fun ResolveDidError.toVerifyJwtError(): VerifyJwtError = when (this) {
    is ResolveDidError.ValidationFailure -> VcSdJwtError.IssuerValidationFailed
    is ResolveDidError.NetworkError -> VcSdJwtError.NetworkError
    is ResolveDidError.Unexpected -> VcSdJwtError.Unexpected(cause)
}

internal fun Throwable.toVerifyVcSdJwtSignatureError(): VerifyVcSdJwtSignatureError =
    VcSdJwtError.InvalidVcSdJwt(this)

internal fun VerifyJwtError.toVerifyVcSdJwtSignatureError(): VerifyVcSdJwtSignatureError = when (this) {
    is VcSdJwtError.DidDocumentDeactivated -> this
    is VcSdJwtError.InvalidJwt -> this
    is VcSdJwtError.IssuerValidationFailed -> this
    is VcSdJwtError.NetworkError -> this
    is VcSdJwtError.Unexpected -> this
}
