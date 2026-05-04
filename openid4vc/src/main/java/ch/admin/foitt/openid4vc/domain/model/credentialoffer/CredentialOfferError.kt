@file:Suppress("TooManyFunctions")

package ch.admin.foitt.openid4vc.domain.model.credentialoffer

import ch.admin.foitt.openid4vc.domain.model.CreateJwkError
import ch.admin.foitt.openid4vc.domain.model.JwkError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError.InvalidSignedMetadata
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError.NetworkInfoError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError.Unexpected
import ch.admin.foitt.openid4vc.domain.model.jwe.CreateJWEError
import ch.admin.foitt.openid4vc.domain.model.jwe.DecryptJWEError
import ch.admin.foitt.openid4vc.domain.model.jwe.JWEError
import ch.admin.foitt.openid4vc.domain.model.jwt.JwtError
import ch.admin.foitt.openid4vc.domain.model.jwt.VerifyJwtSignatureFromDidError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VerifyVcSdJwtSignatureError
import ch.admin.foitt.openid4vc.utils.JsonError
import ch.admin.foitt.openid4vc.utils.JsonParsingError

interface CredentialOfferError {

    data object UnsupportedGrantType :
        FetchVerifiableCredentialError,
        FetchVcSdJwtCredentialError,
        FetchCredentialByConfigError

    data object UnsupportedProofType :
        GetVerifiableCredentialParamsError,
        FetchVerifiableCredentialError,
        FetchVcSdJwtCredentialError,
        FetchCredentialByConfigError

    data object UnsupportedCryptographicSuite :
        GetVerifiableCredentialParamsError,
        FetchVerifiableCredentialError,
        FetchVcSdJwtCredentialError,
        FetchCredentialByConfigError

    data object InvalidCredentialOffer :
        GetVerifiableCredentialParamsError,
        FetchAccessTokenError,
        FetchVerifiableCredentialError,
        FetchDeferredCredentialError,
        FetchVcSdJwtCredentialError,
        FetchCredentialByConfigError

    data object UnsupportedCredentialFormat : FetchCredentialByConfigError
    data object IntegrityCheckFailed :
        FetchVcSdJwtCredentialError,
        FetchCredentialByConfigError

    data object UnknownIssuer :
        FetchVcSdJwtCredentialError,
        FetchCredentialByConfigError

    data object UnsupportedKeyStorageSecurityLevel :
        FetchVerifiableCredentialError,
        FetchVcSdJwtCredentialError,
        FetchCredentialByConfigError

    data object IncompatibleDeviceKeyStorage :
        FetchVerifiableCredentialError,
        FetchVcSdJwtCredentialError,
        FetchCredentialByConfigError

    data class MetadataMisconfiguration(val message: String) :
        FetchVerifiableCredentialError,
        FetchVcSdJwtCredentialError,
        FetchCredentialByConfigError

    data class InvalidSignedMetadata(val message: String) :
        ValidateIssuerMetadataJwtError,
        FetchIssuerConfigurationError,
        FetchIssuerCredentialInfoError,
        GetVerifiableCredentialParamsError

    // region access token response errors
    // see https://www.rfc-editor.org/rfc/rfc6749.html#section-5.2
    // see https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#section-6.3
    data object InvalidRequest :
        FetchAccessTokenError,
        FetchVerifiableCredentialError,
        FetchVcSdJwtCredentialError,
        FetchCredentialByConfigError,
        FetchDeferredCredentialError
    data object InvalidGrant :
        FetchAccessTokenError,
        FetchVerifiableCredentialError,
        FetchDeferredCredentialError,
        FetchVcSdJwtCredentialError,
        FetchCredentialByConfigError
    data object InvalidClient :
        FetchAccessTokenError,
        FetchVerifiableCredentialError,
        FetchDeferredCredentialError,
        FetchVcSdJwtCredentialError,
        FetchCredentialByConfigError
    data object UnauthorizedClient :
        FetchAccessTokenError,
        FetchVerifiableCredentialError,
        FetchDeferredCredentialError,
        FetchVcSdJwtCredentialError,
        FetchCredentialByConfigError
    data object UnauthorizedGrantType :
        FetchAccessTokenError,
        FetchVerifiableCredentialError,
        FetchDeferredCredentialError,
        FetchVcSdJwtCredentialError,
        FetchCredentialByConfigError
    // region end

    // region verifiable credential response errors
    // see https://www.rfc-editor.org/rfc/rfc6750.html#section-3.1
    data object InvalidRequestBearerToken :
        FetchVerifiableCredentialError,
        FetchDeferredCredentialError,
        FetchVcSdJwtCredentialError,
        FetchCredentialByConfigError
    data object InvalidToken :
        FetchVerifiableCredentialError,
        FetchDeferredCredentialError,
        FetchVcSdJwtCredentialError,
        FetchCredentialByConfigError
    data object InsufficientScope :
        FetchVerifiableCredentialError,
        FetchDeferredCredentialError,
        FetchVcSdJwtCredentialError,
        FetchCredentialByConfigError

    // see https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#section-8.3.1.2
    data object InvalidCredentialRequest :
        FetchVerifiableCredentialError,
        FetchDeferredCredentialError,
        FetchVcSdJwtCredentialError,
        FetchCredentialByConfigError
    data object UnknownCredentialConfiguration :
        FetchVerifiableCredentialError,
        FetchDeferredCredentialError,
        FetchVcSdJwtCredentialError,
        FetchCredentialByConfigError
    data object UnknownCredentialIdentifier :
        FetchVerifiableCredentialError,
        FetchDeferredCredentialError,
        FetchVcSdJwtCredentialError,
        FetchCredentialByConfigError
    data object InvalidProof :
        FetchVerifiableCredentialError,
        FetchDeferredCredentialError,
        FetchVcSdJwtCredentialError,
        FetchCredentialByConfigError
    data object InvalidNonce :
        FetchVerifiableCredentialError,
        FetchDeferredCredentialError,
        FetchVcSdJwtCredentialError,
        FetchCredentialByConfigError
    data object InvalidEncryptionParameters :
        FetchVerifiableCredentialError,
        FetchDeferredCredentialError,
        FetchVcSdJwtCredentialError,
        FetchCredentialByConfigError
    data object CredentialRequestDenied :
        FetchVerifiableCredentialError,
        FetchDeferredCredentialError,
        FetchVcSdJwtCredentialError,
        FetchCredentialByConfigError
    // region end

    // region deferred credential response errors (all verifiable credential response errors plus following)
    // see https://www.rfc-editor.org/rfc/rfc6750.html#section-3.1
    // see https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#section-8.3.1.2
    // see https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#section-9.3
    data object InvalidTransactionId : FetchDeferredCredentialError
    // region end

    data object NetworkInfoError :
        ValidateIssuerMetadataJwtError,
        FetchIssuerCredentialInfoError,
        FetchCredentialByConfigError,
        FetchVerifiableCredentialError,
        FetchDeferredCredentialError,
        GetVerifiableCredentialParamsError,
        FetchIssuerConfigurationError,
        FetchNonceError,
        FetchVcSdJwtCredentialError,
        FetchAccessTokenError

    data class Unexpected(val cause: Throwable?) :
        ValidateIssuerMetadataJwtError,
        FetchIssuerCredentialInfoError,
        FetchCredentialByConfigError,
        FetchVerifiableCredentialError,
        FetchDeferredCredentialError,
        GetVerifiableCredentialParamsError,
        FetchIssuerConfigurationError,
        FetchNonceError,
        FetchVcSdJwtCredentialError,
        CreateCredentialRequestError,
        FetchAccessTokenError
}

sealed interface FetchIssuerCredentialInfoError
sealed interface ValidateIssuerMetadataJwtError
sealed interface FetchIssuerConfigurationError
sealed interface FetchCredentialByConfigError
internal sealed interface FetchVcSdJwtCredentialError
sealed interface FetchNonceError
sealed interface FetchAccessTokenError
sealed interface CreateCredentialRequestError
sealed interface GetVerifiableCredentialParamsError
sealed interface FetchVerifiableCredentialError
sealed interface FetchDeferredCredentialError

internal fun FetchAccessTokenError.toFetchVerifiableCredentialError(): FetchVerifiableCredentialError = when (this) {
    is CredentialOfferError.InvalidClient -> this
    is CredentialOfferError.InvalidCredentialOffer -> this
    is CredentialOfferError.InvalidGrant -> this
    is CredentialOfferError.InvalidRequest -> this
    is CredentialOfferError.UnauthorizedClient -> this
    is CredentialOfferError.UnauthorizedGrantType -> this
    is CredentialOfferError.NetworkInfoError -> this
    is CredentialOfferError.Unexpected -> this
}

fun FetchAccessTokenError.toFetchDeferredCredentialError(): FetchDeferredCredentialError = when (this) {
    is CredentialOfferError.InvalidClient -> this
    is CredentialOfferError.InvalidCredentialOffer -> this
    is CredentialOfferError.InvalidGrant -> this
    is CredentialOfferError.InvalidRequest -> this
    is CredentialOfferError.UnauthorizedClient -> this
    is CredentialOfferError.UnauthorizedGrantType -> this
    is CredentialOfferError.NetworkInfoError -> this
    is CredentialOfferError.Unexpected -> this
}

@Suppress("CyclomaticComplexMethod")
internal fun FetchVcSdJwtCredentialError.toFetchCredentialByConfigError(): FetchCredentialByConfigError = when (this) {
    is CredentialOfferError.IntegrityCheckFailed -> this
    is CredentialOfferError.InvalidCredentialOffer -> this
    is CredentialOfferError.NetworkInfoError -> this
    is CredentialOfferError.Unexpected -> this
    is CredentialOfferError.UnsupportedCryptographicSuite -> this
    is CredentialOfferError.UnsupportedGrantType -> this
    is CredentialOfferError.UnsupportedProofType -> this
    is CredentialOfferError.UnknownIssuer -> this
    is CredentialOfferError.UnsupportedKeyStorageSecurityLevel -> this
    is CredentialOfferError.IncompatibleDeviceKeyStorage -> this
    is CredentialOfferError.MetadataMisconfiguration -> this
    is CredentialOfferError.UnauthorizedClient -> this
    is CredentialOfferError.UnauthorizedGrantType -> this
    is CredentialOfferError.InvalidRequestBearerToken -> this
    is CredentialOfferError.InvalidToken -> this
    is CredentialOfferError.InsufficientScope -> this
    is CredentialOfferError.InvalidRequest -> this
    is CredentialOfferError.InvalidGrant -> this
    is CredentialOfferError.InvalidClient -> this
    is CredentialOfferError.InvalidCredentialRequest -> this
    is CredentialOfferError.UnknownCredentialConfiguration -> this
    is CredentialOfferError.UnknownCredentialIdentifier -> this
    is CredentialOfferError.InvalidProof -> this
    is CredentialOfferError.InvalidNonce -> this
    is CredentialOfferError.InvalidEncryptionParameters -> this
    is CredentialOfferError.CredentialRequestDenied -> this
}

internal fun VerifyVcSdJwtSignatureError.toFetchVcSdJwtCredentialError(): FetchVcSdJwtCredentialError = when (this) {
    is VcSdJwtError.InvalidJwt,
    is VcSdJwtError.InvalidVcSdJwt,
    is VcSdJwtError.DidDocumentDeactivated -> CredentialOfferError.IntegrityCheckFailed
    is VcSdJwtError.NetworkError -> CredentialOfferError.NetworkInfoError
    is VcSdJwtError.IssuerValidationFailed -> CredentialOfferError.UnknownIssuer
    is VcSdJwtError.Unexpected -> CredentialOfferError.Unexpected(cause)
}

@Suppress("CyclomaticComplexMethod")
internal fun FetchVerifiableCredentialError.toFetchVcSdJwtCredentialError(): FetchVcSdJwtCredentialError = when (this) {
    is CredentialOfferError.InvalidCredentialOffer -> this
    is CredentialOfferError.NetworkInfoError -> this
    is CredentialOfferError.Unexpected -> this
    is CredentialOfferError.UnsupportedCryptographicSuite -> this
    is CredentialOfferError.UnsupportedGrantType -> this
    is CredentialOfferError.UnsupportedProofType -> this
    is CredentialOfferError.UnsupportedKeyStorageSecurityLevel -> this
    is CredentialOfferError.IncompatibleDeviceKeyStorage -> this
    is CredentialOfferError.MetadataMisconfiguration -> this
    is CredentialOfferError.InvalidRequest -> this
    is CredentialOfferError.UnauthorizedClient -> this
    is CredentialOfferError.UnauthorizedGrantType -> this
    is CredentialOfferError.InvalidRequestBearerToken -> this
    is CredentialOfferError.InvalidToken -> this
    is CredentialOfferError.InsufficientScope -> this
    is CredentialOfferError.InvalidGrant -> this
    is CredentialOfferError.InvalidClient -> this
    is CredentialOfferError.InvalidCredentialRequest -> this
    is CredentialOfferError.UnknownCredentialConfiguration -> this
    is CredentialOfferError.UnknownCredentialIdentifier -> this
    is CredentialOfferError.InvalidProof -> this
    is CredentialOfferError.InvalidNonce -> this
    is CredentialOfferError.InvalidEncryptionParameters -> this
    is CredentialOfferError.CredentialRequestDenied -> this
}

internal fun FetchIssuerCredentialInfoError.toGetVerifiableCredentialParamsError(): GetVerifiableCredentialParamsError = when (this) {
    is CredentialOfferError.InvalidSignedMetadata -> this
    is CredentialOfferError.NetworkInfoError -> this
    is CredentialOfferError.Unexpected -> this
}

internal fun JsonParsingError.toFetchIssuerCredentialInfoError(): FetchIssuerCredentialInfoError = when (this) {
    is JsonError.Unexpected -> CredentialOfferError.Unexpected(this.throwable)
}

internal fun VerifyJwtSignatureFromDidError.toValidateIssuerMetadataJwtError(): ValidateIssuerMetadataJwtError = when (this) {
    JwtError.DidDocumentDeactivated,
    JwtError.IssuerValidationFailed,
    is JwtError.InvalidJwt -> InvalidSignedMetadata("")
    JwtError.NetworkError -> NetworkInfoError
    is JwtError.Unexpected -> Unexpected(throwable)
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

internal fun FetchIssuerConfigurationError.toGetVerifiableCredentialParamsError(): GetVerifiableCredentialParamsError = when (this) {
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
