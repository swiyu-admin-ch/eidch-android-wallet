@file:Suppress("TooManyFunctions")

package ch.admin.foitt.wallet.platform.trustRegistry.domain.model

import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VerifyJwtError
import ch.admin.foitt.wallet.platform.utils.JsonError
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import timber.log.Timber

interface TrustRegistryError {
    data object InvalidTrustStatus :
        FetchTrustStatementForIssuanceError,
        FetchTrustStatementForVerificationError,
        ProcessMetadataV1TrustStatementError,
        ProcessIdentityV1TrustStatementError,
        FetchVcSchemaTrustStatusError

    data class Unexpected(val cause: Throwable?) :
        FetchTrustStatementForIssuanceError,
        FetchTrustStatementForVerificationError,
        ProcessMetadataV1TrustStatementError,
        ProcessIdentityV1TrustStatementError,
        FetchVcSchemaTrustStatusError,
        GetTrustDomainFromDidError,
        GetTrustUrlFromDidError,
        TrustStatementRepositoryError,
        ValidateTrustStatementError
}

sealed interface FetchTrustStatementForIssuanceError
sealed interface FetchTrustStatementForVerificationError
sealed interface ProcessMetadataV1TrustStatementError
sealed interface ProcessIdentityV1TrustStatementError
sealed interface FetchVcSchemaTrustStatusError
sealed interface GetTrustDomainFromDidError {
    data class NoTrustRegistryMapping(val message: String) : GetTrustDomainFromDidError
}

sealed interface GetTrustUrlFromDidError
sealed interface TrustStatementRepositoryError
sealed interface ValidateTrustStatementError

fun GetTrustDomainFromDidError.toGetTrustUrlFromDidError(): GetTrustUrlFromDidError = when (this) {
    is TrustRegistryError.Unexpected -> this
    is GetTrustDomainFromDidError.NoTrustRegistryMapping -> {
        Timber.w(message = this.message)
        TrustRegistryError.Unexpected(null)
    }
}

fun GetTrustUrlFromDidError.toProcessMetadataV1TrustStatementError(): ProcessMetadataV1TrustStatementError = when (this) {
    is TrustRegistryError.Unexpected -> this
}

fun TrustStatementRepositoryError.toProcessMetadataV1TrustStatementError(): ProcessMetadataV1TrustStatementError = when (this) {
    is TrustRegistryError.Unexpected -> this
}

fun Throwable.toProcessMetadataV1TrustStatementError(message: String): ProcessMetadataV1TrustStatementError {
    Timber.e(t = this, message = message)
    return TrustRegistryError.Unexpected(this)
}

fun JsonParsingError.toProcessMetadataV1TrustStatementError(): ProcessMetadataV1TrustStatementError = when (this) {
    is JsonError.Unexpected -> TrustRegistryError.Unexpected(throwable)
}

fun GetTrustUrlFromDidError.toProcessIdentityV1TrustStatementError(): ProcessIdentityV1TrustStatementError = when (this) {
    is TrustRegistryError.Unexpected -> this
}

fun TrustStatementRepositoryError.toProcessIdentityV1TrustStatementError(): ProcessIdentityV1TrustStatementError = when (this) {
    is TrustRegistryError.Unexpected -> this
}

fun Throwable.toProcessIdentityV1TrustStatementError(message: String): ProcessIdentityV1TrustStatementError {
    Timber.e(t = this, message = message)
    return TrustRegistryError.Unexpected(this)
}

fun JsonParsingError.toProcessIdentityV1TrustStatementError(): ProcessIdentityV1TrustStatementError = when (this) {
    is JsonError.Unexpected -> TrustRegistryError.Unexpected(throwable)
}

fun GetTrustUrlFromDidError.toFetchVcSchemaTrustStatusError(): FetchVcSchemaTrustStatusError = when (this) {
    is TrustRegistryError.Unexpected -> this
}

fun TrustStatementRepositoryError.toFetchVcSchemaTrustStatusError(): FetchVcSchemaTrustStatusError = when (this) {
    is TrustRegistryError.Unexpected -> this
}

fun Throwable.toFetchVcSchemaTrustStatusError(message: String): FetchVcSchemaTrustStatusError {
    Timber.e(t = this, message = message)
    return TrustRegistryError.Unexpected(this)
}

fun Throwable.toTrustStatementRepositoryError(message: String): TrustStatementRepositoryError {
    Timber.e(t = this, message = message)
    return TrustRegistryError.Unexpected(this)
}

fun Throwable.toGetTrustDomainFromDidError(message: String): GetTrustDomainFromDidError {
    Timber.e(t = this, message = message)
    return TrustRegistryError.Unexpected(this)
}

fun Throwable.toGetTrustUrlFromDidError(message: String): GetTrustUrlFromDidError {
    Timber.e(t = this, message = message)
    return TrustRegistryError.Unexpected(this)
}

fun VerifyJwtError.toValidateTrustStatementError(): ValidateTrustStatementError = when (this) {
    VcSdJwtError.NetworkError,
    VcSdJwtError.InvalidJwt,
    VcSdJwtError.IssuerValidationFailed -> TrustRegistryError.Unexpected(null)
    VcSdJwtError.DidDocumentDeactivated -> TrustRegistryError.Unexpected(null)
    is VcSdJwtError.Unexpected -> TrustRegistryError.Unexpected(cause)
}

fun Throwable.toValidateTrustStatementError(message: String): ValidateTrustStatementError {
    Timber.e(t = this, message = message)
    return TrustRegistryError.Unexpected(this)
}
