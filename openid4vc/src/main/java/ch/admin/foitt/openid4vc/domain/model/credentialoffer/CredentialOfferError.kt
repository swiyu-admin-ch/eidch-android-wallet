@file:Suppress("TooManyFunctions")

package ch.admin.foitt.openid4vc.domain.model.credentialoffer

import ch.admin.foitt.openid4vc.domain.model.CreateJwkError
import ch.admin.foitt.openid4vc.domain.model.JwkError
import ch.admin.foitt.openid4vc.domain.model.jwe.CreateJWEError
import ch.admin.foitt.openid4vc.domain.model.jwe.DecryptJWEError
import ch.admin.foitt.openid4vc.domain.model.jwe.JWEError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VerifyJwtError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VerifyVcSdJwtSignatureError
import ch.admin.foitt.openid4vc.utils.JsonError
import ch.admin.foitt.openid4vc.utils.JsonParsingError

interface CredentialOfferError {
    data object InvalidGrant :
        FetchCredentialByConfigError, FetchVerifiableCredentialError, FetchCredentialError

    data object UnsupportedGrantType :
        FetchCredentialByConfigError, FetchVerifiableCredentialError, FetchCredentialError

    data object UnsupportedCredentialIdentifier : FetchCredentialByConfigError
    data object UnsupportedProofType :
        FetchCredentialByConfigError,
        FetchVerifiableCredentialError,
        PrepareFetchVerifiableCredentialError,
        FetchCredentialError

    data object UnsupportedCryptographicSuite :
        FetchCredentialByConfigError,
        FetchVerifiableCredentialError,
        PrepareFetchVerifiableCredentialError,
        FetchCredentialError

    data object InvalidCredentialOffer :
        FetchCredentialByConfigError,
        FetchVerifiableCredentialError,
        PrepareFetchVerifiableCredentialError,
        FetchCredentialError

    data object UnsupportedCredentialFormat : FetchCredentialByConfigError
    data object IntegrityCheckFailed : FetchCredentialByConfigError, FetchCredentialError
    data object UnknownIssuer : FetchCredentialByConfigError, FetchCredentialError
    data object UnsupportedKeyStorageSecurityLevel : FetchVerifiableCredentialError, FetchCredentialError, FetchCredentialByConfigError
    data object IncompatibleDeviceKeyStorage : FetchVerifiableCredentialError, FetchCredentialError, FetchCredentialByConfigError
    data object CredentialRequestDenied : FetchDeferredCredentialError
    data object InvalidTransactionId : FetchDeferredCredentialError
    data object InvalidRequest : FetchDeferredCredentialError
    data class MetadataMisconfiguration(val message: String) :
        FetchVerifiableCredentialError,
        FetchCredentialError,
        FetchCredentialByConfigError

    data class InvalidSignedMetadata(val message: String) :
        ValidateIssuerMetadataJwtError,
        FetchIssuerCredentialInfoError,
        FetchIssuerConfigurationError,
        PrepareFetchVerifiableCredentialError

    data object NetworkInfoError :
        ValidateIssuerMetadataJwtError,
        FetchIssuerCredentialInfoError,
        FetchCredentialByConfigError,
        FetchVerifiableCredentialError,
        FetchDeferredCredentialError,
        PrepareFetchVerifiableCredentialError,
        FetchIssuerConfigurationError,
        FetchNonceError,
        FetchCredentialError

    data class Unexpected(val cause: Throwable?) :
        ValidateIssuerMetadataJwtError,
        FetchIssuerCredentialInfoError,
        FetchCredentialByConfigError,
        FetchVerifiableCredentialError,
        FetchDeferredCredentialError,
        PrepareFetchVerifiableCredentialError,
        FetchIssuerConfigurationError,
        FetchNonceError,
        FetchCredentialError,
        CreateCredentialRequestError
}

sealed interface FetchIssuerCredentialInfoError
sealed interface FetchCredentialByConfigError
internal sealed interface FetchCredentialError
sealed interface FetchVerifiableCredentialError
sealed interface FetchDeferredCredentialError
sealed interface PrepareFetchVerifiableCredentialError
sealed interface FetchIssuerConfigurationError
sealed interface FetchNonceError
sealed interface ValidateIssuerMetadataJwtError
sealed interface CreateCredentialRequestError

internal fun FetchCredentialError.toFetchCredentialByConfigError(): FetchCredentialByConfigError = when (this) {
    is CredentialOfferError.IntegrityCheckFailed -> this
    is CredentialOfferError.InvalidCredentialOffer -> this
    is CredentialOfferError.InvalidGrant -> this
    is CredentialOfferError.NetworkInfoError -> this
    is CredentialOfferError.Unexpected -> this
    is CredentialOfferError.UnsupportedCryptographicSuite -> this
    is CredentialOfferError.UnsupportedGrantType -> this
    is CredentialOfferError.UnsupportedProofType -> this
    is CredentialOfferError.UnknownIssuer -> this
    is CredentialOfferError.UnsupportedKeyStorageSecurityLevel -> this
    is CredentialOfferError.IncompatibleDeviceKeyStorage -> this
    is CredentialOfferError.MetadataMisconfiguration -> this
}

internal fun VerifyVcSdJwtSignatureError.toFetchCredentialError(): FetchCredentialError = when (this) {
    is VcSdJwtError.InvalidJwt,
    is VcSdJwtError.InvalidVcSdJwt,
    is VcSdJwtError.DidDocumentDeactivated -> CredentialOfferError.IntegrityCheckFailed
    is VcSdJwtError.NetworkError -> CredentialOfferError.NetworkInfoError
    is VcSdJwtError.IssuerValidationFailed -> CredentialOfferError.UnknownIssuer
    is VcSdJwtError.Unexpected -> CredentialOfferError.Unexpected(cause)
}

internal fun FetchVerifiableCredentialError.toFetchCredentialError(): FetchCredentialError = when (this) {
    is CredentialOfferError.InvalidCredentialOffer -> this
    is CredentialOfferError.InvalidGrant -> this
    is CredentialOfferError.NetworkInfoError -> this
    is CredentialOfferError.Unexpected -> this
    is CredentialOfferError.UnsupportedCryptographicSuite -> this
    is CredentialOfferError.UnsupportedGrantType -> this
    is CredentialOfferError.UnsupportedProofType -> this
    is CredentialOfferError.UnsupportedKeyStorageSecurityLevel -> this
    is CredentialOfferError.IncompatibleDeviceKeyStorage -> this
    is CredentialOfferError.MetadataMisconfiguration -> this
}

internal fun FetchIssuerCredentialInfoError.toPrepareFetchVerifiableCredentialError(): PrepareFetchVerifiableCredentialError = when (this) {
    is CredentialOfferError.InvalidSignedMetadata -> this
    is CredentialOfferError.NetworkInfoError -> this
    is CredentialOfferError.Unexpected -> this
}

internal fun JsonParsingError.toFetchIssuerCredentialInfoError(): FetchIssuerCredentialInfoError = when (this) {
    is JsonError.Unexpected -> CredentialOfferError.Unexpected(this.throwable)
}

internal fun VerifyJwtError.toValidateIssuerMetadataJwtError(): ValidateIssuerMetadataJwtError = when (this) {
    VcSdJwtError.InvalidJwt -> CredentialOfferError.InvalidSignedMetadata("JWT signature invalid")
    VcSdJwtError.DidDocumentDeactivated -> CredentialOfferError.InvalidSignedMetadata("Did document deactivated")

    VcSdJwtError.NetworkError -> CredentialOfferError.NetworkInfoError
    VcSdJwtError.IssuerValidationFailed -> CredentialOfferError.InvalidSignedMetadata("Could not resolve did")
    is VcSdJwtError.Unexpected -> CredentialOfferError.Unexpected(cause)
}

internal fun ValidateIssuerMetadataJwtError.toFetchIssuerCredentialInfoError(): FetchIssuerCredentialInfoError = when (this) {
    is CredentialOfferError.InvalidSignedMetadata -> this
    is CredentialOfferError.NetworkInfoError -> this
    is CredentialOfferError.Unexpected -> this
}

internal fun ValidateIssuerMetadataJwtError.toFetchIssuerConfigurationError(): FetchIssuerConfigurationError = when (this) {
    is CredentialOfferError.InvalidSignedMetadata -> this
    is CredentialOfferError.NetworkInfoError -> this
    is CredentialOfferError.Unexpected -> this
}

internal fun JsonParsingError.toFetchIssuerConfigurationError(): FetchIssuerConfigurationError = when (this) {
    is JsonError.Unexpected -> CredentialOfferError.Unexpected(this.throwable)
}

internal fun FetchIssuerConfigurationError.toPrepareFetchVerifiableCredentialError(): PrepareFetchVerifiableCredentialError = when (this) {
    is CredentialOfferError.InvalidSignedMetadata -> this
    is CredentialOfferError.NetworkInfoError -> this
    is CredentialOfferError.Unexpected -> this
}

internal fun FetchNonceError.toFetchVerifiableCredentialError(): FetchVerifiableCredentialError = when (this) {
    is CredentialOfferError.NetworkInfoError -> this
    is CredentialOfferError.Unexpected -> this
}

internal fun CreateJwkError.toFetchVerifiableCredentialError(): FetchVerifiableCredentialError = when (this) {
    is JwkError.UnsupportedCryptographicSuite -> CredentialOfferError.UnsupportedCryptographicSuite
    is JwkError.Unexpected -> CredentialOfferError.Unexpected(cause)
}

internal fun JsonParsingError.toCreateCredentialRequestError(): CreateCredentialRequestError = when (this) {
    is JsonError.Unexpected -> CredentialOfferError.Unexpected(this.throwable)
}

internal fun CreateCredentialRequestError.toFetchVerifiableCredentialError(): FetchVerifiableCredentialError = when (this) {
    is CredentialOfferError.Unexpected -> this
}

internal fun DecryptJWEError.toFetchVerifiableCredentialError(): FetchVerifiableCredentialError = when (this) {
    is JWEError.Unexpected -> CredentialOfferError.Unexpected(throwable)
}

internal fun DecryptJWEError.toFetchDeferredCredentialError(): FetchDeferredCredentialError = when (this) {
    is JWEError.Unexpected -> CredentialOfferError.Unexpected(throwable)
}

internal fun CreateJWEError.toCreateCredentialRequestError(): CreateCredentialRequestError = when (this) {
    is JWEError.Unexpected -> CredentialOfferError.Unexpected(throwable)
}
