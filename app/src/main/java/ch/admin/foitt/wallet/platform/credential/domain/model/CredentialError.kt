@file:Suppress("TooManyFunctions")

package ch.admin.foitt.wallet.platform.credential.domain.model

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CreateCredentialRequestError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchCredentialByConfigError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchIssuerCredentialInfoError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.PrepareFetchVerifiableCredentialError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VerifyVcSdJwtSignatureError
import ch.admin.foitt.wallet.platform.batch.domain.error.DeleteBundleItemsByAmountError
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError.IncompatibleDeviceKeyStorage
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError.IntegrityCheckFailed
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError.InvalidCredentialOffer
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError.InvalidGenerateMetadataClaims
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError.InvalidGrant
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError.InvalidJsonScheme
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError.InvalidSignedMetadata
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError.MetadataMisconfiguration
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError.NetworkError
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError.Unexpected
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError.UnknownIssuer
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError.UnsupportedCredentialFormat
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError.UnsupportedCredentialIdentifier
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError.UnsupportedCryptographicSuite
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError.UnsupportedGrantType
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError.UnsupportedKeyStorageSecurityLevel
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError.UnsupportedProofType
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CredentialPresentationError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.GetCompatibleCredentialsError
import ch.admin.foitt.wallet.platform.holderBinding.domain.model.GenerateProofKeyPairError
import ch.admin.foitt.wallet.platform.holderBinding.domain.model.HolderBindingError
import ch.admin.foitt.wallet.platform.oca.domain.model.FetchVcMetadataByFormatError
import ch.admin.foitt.wallet.platform.oca.domain.model.GenerateOcaDisplaysError
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaError
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.model.GetPayloadEncryptionTypeError
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.model.PayloadEncryptionError
import ch.admin.foitt.wallet.platform.ssi.domain.model.BundleItemRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialOfferRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialWithKeyBindingRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.DeferredCredentialRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.DeleteBundleItemError
import ch.admin.foitt.wallet.platform.ssi.domain.model.RawCredentialDataRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.utils.JsonError
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import timber.log.Timber
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError as OpenIdCredentialOfferError

sealed interface CredentialError {
    data object InvalidGrant : FetchCredentialError
    data object UnsupportedGrantType : FetchCredentialError
    data object UnsupportedCredentialIdentifier : FetchCredentialError
    data object UnsupportedProofType : FetchCredentialError
    data object UnsupportedCryptographicSuite : FetchCredentialError
    data object InvalidCredentialOffer : FetchCredentialError, RefreshDeferredCredentialsError
    data object UnsupportedCredentialFormat : FetchCredentialError, RefreshDeferredCredentialsError
    data object CredentialParsingError : FetchCredentialError
    data object IntegrityCheckFailed : FetchCredentialError, RefreshDeferredCredentialsError
    data object InvalidGenerateMetadataClaims :
        FetchCredentialError,
        GenerateCredentialDisplaysError,
        GenerateMetadataDisplaysError,
        RefreshDeferredCredentialsError

    data object InvalidJsonScheme : FetchCredentialError, RefreshDeferredCredentialsError
    data class MetadataMisconfiguration(val message: String) : FetchCredentialError
    data class InvalidSignedMetadata(
        val message: String
    ) : FetchCredentialError, FetchExistingIssuerCredentialInfoError, RefreshDeferredCredentialsError

    data object DatabaseError : FetchCredentialError
    data object NetworkError :
        FetchCredentialError,
        RefreshDeferredCredentialsError,
        FetchExistingIssuerCredentialInfoError

    data object UnknownIssuer : FetchCredentialError, RefreshDeferredCredentialsError
    data object UnsupportedKeyStorageSecurityLevel :
        FetchCredentialError,
        RefreshDeferredCredentialsError
    data object IncompatibleDeviceKeyStorage :
        FetchCredentialError,
        RefreshDeferredCredentialsError
    data object InvalidIssuerCredentialInfo :
        FetchCredentialError,
        RefreshDeferredCredentialsError

    data class Unexpected(val cause: Throwable?) :
        FetchCredentialError,
        GetAllAnyCredentialsByCredentialIdError,
        GetAnyCredentialsError,
        AnyCredentialError,
        RefreshDeferredCredentialsError,
        FetchExistingIssuerCredentialInfoError,
        MapToCredentialDisplayDataError,
        GenerateCredentialDisplaysError,
        GenerateMetadataDisplaysError,
        KeyBindingError
}

sealed interface FetchCredentialError
sealed interface GetAllAnyCredentialsByCredentialIdError
sealed interface GetAnyCredentialsError
sealed interface AnyCredentialError
sealed interface MapToCredentialDisplayDataError
sealed interface GenerateCredentialDisplaysError
sealed interface GenerateMetadataDisplaysError
sealed interface KeyBindingError
sealed interface RefreshDeferredCredentialsError
sealed interface FetchExistingIssuerCredentialInfoError

fun FetchIssuerCredentialInfoError.toFetchCredentialError(): FetchCredentialError = when (this) {
    is OpenIdCredentialOfferError.InvalidSignedMetadata -> InvalidSignedMetadata(message)
    OpenIdCredentialOfferError.NetworkInfoError -> NetworkError
    is OpenIdCredentialOfferError.Unexpected -> Unexpected(cause)
}

@Suppress("CyclomaticComplexMethod")
fun FetchCredentialByConfigError.toFetchCredentialError(): FetchCredentialError = when (this) {
    is OpenIdCredentialOfferError.InvalidGrant -> InvalidGrant
    is OpenIdCredentialOfferError.InvalidCredentialOffer -> InvalidCredentialOffer
    is OpenIdCredentialOfferError.UnsupportedCryptographicSuite -> UnsupportedCryptographicSuite
    is OpenIdCredentialOfferError.UnsupportedGrantType -> UnsupportedGrantType
    is OpenIdCredentialOfferError.UnsupportedProofType -> UnsupportedProofType
    is OpenIdCredentialOfferError.IntegrityCheckFailed -> IntegrityCheckFailed
    is OpenIdCredentialOfferError.NetworkInfoError -> NetworkError
    is OpenIdCredentialOfferError.UnsupportedCredentialIdentifier -> UnsupportedCredentialIdentifier
    is OpenIdCredentialOfferError.UnsupportedCredentialFormat -> UnsupportedCredentialFormat
    is OpenIdCredentialOfferError.Unexpected -> Unexpected(cause)
    is OpenIdCredentialOfferError.UnknownIssuer -> UnknownIssuer
    is OpenIdCredentialOfferError.UnsupportedKeyStorageSecurityLevel -> UnsupportedKeyStorageSecurityLevel
    is OpenIdCredentialOfferError.IncompatibleDeviceKeyStorage -> IncompatibleDeviceKeyStorage
    is OpenIdCredentialOfferError.MetadataMisconfiguration -> MetadataMisconfiguration(message)
}

fun PrepareFetchVerifiableCredentialError.toFetchCredentialError(): FetchCredentialError = when (this) {
    is OpenIdCredentialOfferError.UnsupportedProofType -> UnsupportedProofType
    is OpenIdCredentialOfferError.UnsupportedCryptographicSuite -> UnsupportedCryptographicSuite
    is OpenIdCredentialOfferError.InvalidCredentialOffer -> InvalidCredentialOffer
    is OpenIdCredentialOfferError.InvalidSignedMetadata -> InvalidSignedMetadata(message)
    is OpenIdCredentialOfferError.NetworkInfoError -> NetworkError
    is OpenIdCredentialOfferError.Unexpected -> Unexpected(cause)
}

fun GenerateProofKeyPairError.toFetchCredentialError(): FetchCredentialError = when (this) {
    is HolderBindingError.InvalidProofKeyAttestation -> Unexpected(null)
    is HolderBindingError.UnsupportedCryptographicSuite -> InvalidCredentialOffer
    is HolderBindingError.IncompatibleDeviceProofKeyStorage -> IncompatibleDeviceKeyStorage
    is HolderBindingError.UnsupportedProofKeyStorageSecurityLevel -> UnsupportedKeyStorageSecurityLevel
    is HolderBindingError.Unexpected -> Unexpected(throwable)
}

fun JsonParsingError.toGenerateMetadataDisplaysError(): GenerateMetadataDisplaysError = when (this) {
    is JsonError.Unexpected -> Unexpected(throwable)
}

fun Throwable.toGenerateCredentialDisplaysError(message: String): GenerateCredentialDisplaysError {
    Timber.e(t = this, message = message)
    return Unexpected(this)
}

fun DeleteBundleItemError.toFetchCredentialError(): FetchCredentialError = when (this) {
    is SsiError.Unexpected -> Unexpected(cause)
}

fun BundleItemRepositoryError.toFetchCredentialError(): FetchCredentialError = when (this) {
    is SsiError.Unexpected -> Unexpected(cause)
}

fun CredentialOfferRepositoryError.toFetchCredentialError(): FetchCredentialError = when (this) {
    is SsiError.Unexpected -> Unexpected(cause)
}

fun AnyCredentialError.toGetCompatibleCredentialsError(): GetCompatibleCredentialsError = when (this) {
    is Unexpected -> CredentialPresentationError.Unexpected(cause)
}

fun AnyCredentialError.toGetAnyCredentialsError(): GetAnyCredentialsError = when (this) {
    is Unexpected -> this
}

fun AnyCredentialError.toGetAllAnyCredentialsByCredentialIdError(): GetAllAnyCredentialsByCredentialIdError = when (this) {
    is Unexpected -> this
}

fun Throwable.toAnyCredentialError(message: String): AnyCredentialError {
    Timber.e(t = this, message = message)
    return Unexpected(this)
}

fun FetchVcMetadataByFormatError.toFetchCredentialError(): FetchCredentialError = when (this) {
    is OcaError.InvalidOca -> InvalidCredentialOffer
    is OcaError.NetworkError -> NetworkError
    is OcaError.Unexpected -> Unexpected(cause)
    OcaError.InvalidJsonScheme -> InvalidJsonScheme
    OcaError.UnsupportedCredentialFormat -> UnsupportedCredentialFormat
}

fun GenerateMetadataDisplaysError.toGenerateCredentialDisplaysError(): GenerateCredentialDisplaysError = when (this) {
    is InvalidGenerateMetadataClaims -> this
    is Unexpected -> this
}

fun GenerateOcaDisplaysError.toGenerateCredentialDisplaysError(): GenerateCredentialDisplaysError = when (this) {
    is OcaError.InvalidRootCaptureBase -> InvalidGenerateMetadataClaims
    is OcaError.Unexpected -> Unexpected(cause)
}

fun BundleItemRepositoryError.toGetAnyCredentialsError(): GetAnyCredentialsError = when (this) {
    is SsiError.Unexpected -> Unexpected(cause)
}

fun BundleItemRepositoryError.toGetAllAnyCredentialsByCredentialIdError(): GetAllAnyCredentialsByCredentialIdError = when (this) {
    is SsiError.Unexpected -> Unexpected(cause)
}

fun CredentialWithKeyBindingRepositoryError.toGetAnyCredentialsError(): GetAnyCredentialsError = when (this) {
    is SsiError.Unexpected -> Unexpected(cause)
}

fun CredentialWithKeyBindingRepositoryError.toGetAllAnyCredentialsByCredentialIdError(): GetAllAnyCredentialsByCredentialIdError =
    when (this) {
        is SsiError.Unexpected -> Unexpected(cause)
    }

fun Throwable.toKeyBindingError(message: String): KeyBindingError {
    Timber.e(t = this, message = message)
    return Unexpected(this)
}

fun KeyBindingError.toAnyCredentialError(): AnyCredentialError = when (this) {
    is Unexpected -> this
}

fun DeferredCredentialRepositoryError.toRefreshDeferredCredentialsError(): RefreshDeferredCredentialsError = when (this) {
    is SsiError.Unexpected -> Unexpected(cause)
}

fun VerifyVcSdJwtSignatureError.toRefreshDeferredCredentialsError(): RefreshDeferredCredentialsError = when (this) {
    VcSdJwtError.DidDocumentDeactivated,
    VcSdJwtError.InvalidJwt,
    is VcSdJwtError.InvalidVcSdJwt -> IntegrityCheckFailed

    VcSdJwtError.IssuerValidationFailed -> UnknownIssuer
    VcSdJwtError.NetworkError -> NetworkError
    is VcSdJwtError.Unexpected -> Unexpected(cause)
}

fun FetchExistingIssuerCredentialInfoError.toRefreshDeferredCredentialsError(): RefreshDeferredCredentialsError = when (this) {
    is NetworkError -> this
    is Unexpected -> this
    is InvalidSignedMetadata -> this
}

fun FetchVcMetadataByFormatError.toRefreshDeferredCredentialsError(): RefreshDeferredCredentialsError = when (this) {
    OcaError.InvalidOca -> InvalidCredentialOffer
    OcaError.InvalidJsonScheme -> InvalidJsonScheme
    OcaError.UnsupportedCredentialFormat -> UnsupportedCredentialFormat
    OcaError.NetworkError -> NetworkError
    is OcaError.Unexpected -> Unexpected(cause)
}

fun GenerateCredentialDisplaysError.toRefreshDeferredCredentialsError(): RefreshDeferredCredentialsError = when (this) {
    is InvalidGenerateMetadataClaims -> this
    is Unexpected -> this
}

fun GenerateCredentialDisplaysError.toFetchCredentialError(): FetchCredentialError = when (this) {
    is InvalidGenerateMetadataClaims -> this
    is Unexpected -> this
}

fun CredentialOfferRepositoryError.toRefreshDeferredCredentialsError(): RefreshDeferredCredentialsError = when (this) {
    is SsiError.Unexpected -> Unexpected(cause)
}

fun FetchIssuerCredentialInfoError.toFetchExistingIssuerCredentialInfoError(): FetchExistingIssuerCredentialInfoError = when (this) {
    OpenIdCredentialOfferError.NetworkInfoError -> NetworkError
    is OpenIdCredentialOfferError.Unexpected -> Unexpected(cause)
    is OpenIdCredentialOfferError.InvalidSignedMetadata -> InvalidSignedMetadata(message)
}

fun CreateCredentialRequestError.toRefreshDeferredCredentialsError(): RefreshDeferredCredentialsError = when (this) {
    is OpenIdCredentialOfferError.Unexpected -> Unexpected(cause)
}

fun GetPayloadEncryptionTypeError.toFetchCredentialError(): FetchCredentialError = when (this) {
    is PayloadEncryptionError.IncompatibleDeviceProofKeyStorage -> IncompatibleDeviceKeyStorage
    is PayloadEncryptionError.UnsupportedProofKeyStorageSecurityLevel -> UnsupportedKeyStorageSecurityLevel
    is PayloadEncryptionError.Unexpected -> Unexpected(throwable)
}

fun GetPayloadEncryptionTypeError.toRefreshDeferredCredentialsError(): RefreshDeferredCredentialsError = when (this) {
    is PayloadEncryptionError.IncompatibleDeviceProofKeyStorage -> IncompatibleDeviceKeyStorage
    is PayloadEncryptionError.UnsupportedProofKeyStorageSecurityLevel -> UnsupportedKeyStorageSecurityLevel
    is PayloadEncryptionError.Unexpected -> Unexpected(throwable)
}

fun CredentialRepositoryError.toFetchExistingIssuerCredentialInfoError(): FetchExistingIssuerCredentialInfoError = when (this) {
    is SsiError.Unexpected -> Unexpected(cause)
}

fun RawCredentialDataRepositoryError.toRefreshDeferredCredentialsError(): RefreshDeferredCredentialsError = when (this) {
    is SsiError.Unexpected -> Unexpected(cause)
}

fun DeleteBundleItemsByAmountError.toFetchCredentialError(): FetchCredentialError = when (this) {
    is DeleteBundleItemsByAmountError.Unexpected -> Unexpected(cause)
}
