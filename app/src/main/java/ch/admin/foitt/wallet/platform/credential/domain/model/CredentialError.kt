@file:Suppress("TooManyFunctions")

package ch.admin.foitt.wallet.platform.credential.domain.model

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CreateCredentialRequestError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchAccessTokenError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchCredentialByConfigError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchDeferredCredentialError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchIssuerConfigurationError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchIssuerCredentialInfoError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.GetVerifiableCredentialParamsError
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
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError.UnsupportedCryptographicSuite
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError.UnsupportedGrantType
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError.UnsupportedImageFormat
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError.UnsupportedKeyStorageSecurityLevel
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError.UnsupportedProofType
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CredentialPresentationError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.GetCompatibleCredentialsError
import ch.admin.foitt.wallet.platform.holderBinding.domain.model.GenerateProofKeyPairError
import ch.admin.foitt.wallet.platform.holderBinding.domain.model.HolderBindingError
import ch.admin.foitt.wallet.platform.imageValidation.domain.model.ImageValidationError
import ch.admin.foitt.wallet.platform.imageValidation.domain.model.ValidateImageError
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
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import timber.log.Timber
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError as OpenIdCredentialOfferError

sealed interface CredentialError {
    data object InvalidGrant : FetchCredentialError
    data object UnsupportedGrantType : FetchCredentialError
    data object UnsupportedCredentialIdentifier : FetchCredentialError
    data object UnsupportedProofType : FetchCredentialError
    data object UnsupportedCryptographicSuite : FetchCredentialError
    data object InvalidCredentialOffer :
        FetchCredentialError,
        UpdateDeferredCredentialError,
        SaveCredentialFromDeferredError,
        FetchAndUpdateDeferredCredentialError

    data object UnsupportedCredentialFormat :
        FetchCredentialError,
        SaveCredentialFromDeferredError
    data object CredentialParsingError : FetchCredentialError
    data object IntegrityCheckFailed :
        FetchCredentialError,
        SaveCredentialFromDeferredError
    data object InvalidGenerateMetadataClaims :
        FetchCredentialError,
        GenerateCredentialDisplaysError,
        GenerateMetadataDisplaysError,
        SaveCredentialFromDeferredError,
        UpdateDeferredCredentialError,
        FetchAndUpdateDeferredCredentialError

    data object InvalidJsonScheme :
        FetchCredentialError,
        SaveCredentialFromDeferredError

    data object UnsupportedImageFormat :
        GenerateMetadataDisplaysError,
        GenerateCredentialDisplaysError,
        FetchCredentialError,
        UpdateDeferredCredentialError,
        SaveCredentialFromDeferredError,
        FetchAndUpdateDeferredCredentialError

    data class MetadataMisconfiguration(val message: String) : FetchCredentialError
    data class InvalidSignedMetadata(val message: String) :
        FetchCredentialError,
        FetchExistingIssuerCredentialInfoError,
        UpdateDeferredCredentialError,
        FetchAndUpdateDeferredCredentialError

    data object DatabaseError : FetchCredentialError

    data object UnknownIssuer :
        FetchCredentialError,
        SaveCredentialFromDeferredError

    data object UnsupportedKeyStorageSecurityLevel :
        FetchCredentialError,
        FetchAndUpdateDeferredCredentialError

    data object IncompatibleDeviceKeyStorage :
        FetchCredentialError,
        FetchAndUpdateDeferredCredentialError

    data object InvalidIssuerCredentialInfo :
        FetchCredentialError,
        UpdateDeferredCredentialError,
        FetchAndUpdateDeferredCredentialError

    data object CredentialRequestDenied : FetchCredentialError
    data object InsufficientScope : FetchCredentialError
    data object InvalidClient : FetchCredentialError
    data object InvalidCredentialRequest : FetchCredentialError
    data object InvalidEncryptionParameters : FetchCredentialError
    data object InvalidNonce : FetchCredentialError
    data object InvalidProof : FetchCredentialError
    data object InvalidRequest : FetchCredentialError
    data object InvalidRequestBearerToken : FetchCredentialError
    data object InvalidToken : FetchCredentialError
    data object UnauthorizedClient : FetchCredentialError
    data object UnauthorizedGrantType : FetchCredentialError
    data object UnknownCredentialConfiguration : FetchCredentialError
    data object UnknownCredentialIdentifier : FetchCredentialError

    data object NetworkError :
        FetchCredentialError,
        UpdateDeferredCredentialError,
        FetchExistingIssuerCredentialInfoError,
        SaveCredentialFromDeferredError,
        FetchAndUpdateDeferredCredentialError

    data class Unexpected(val cause: Throwable?) :
        FetchCredentialError,
        GetAllAnyCredentialsByCredentialIdError,
        GetAnyCredentialsError,
        AnyCredentialError,
        FetchAndUpdateDeferredCredentialError,
        UpdateDeferredCredentialError,
        SaveCredentialFromDeferredError,
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
sealed interface SaveCredentialFromDeferredError
sealed interface UpdateDeferredCredentialError
sealed interface FetchAndUpdateDeferredCredentialError

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
    is OpenIdCredentialOfferError.UnsupportedCredentialFormat -> UnsupportedCredentialFormat
    is OpenIdCredentialOfferError.Unexpected -> Unexpected(cause)
    is OpenIdCredentialOfferError.UnknownIssuer -> UnknownIssuer
    is OpenIdCredentialOfferError.UnsupportedKeyStorageSecurityLevel -> UnsupportedKeyStorageSecurityLevel
    is OpenIdCredentialOfferError.IncompatibleDeviceKeyStorage -> IncompatibleDeviceKeyStorage
    is OpenIdCredentialOfferError.MetadataMisconfiguration -> MetadataMisconfiguration(message)
    OpenIdCredentialOfferError.CredentialRequestDenied -> CredentialError.CredentialRequestDenied
    OpenIdCredentialOfferError.InsufficientScope -> CredentialError.InsufficientScope
    OpenIdCredentialOfferError.InvalidClient -> CredentialError.InvalidClient
    OpenIdCredentialOfferError.InvalidCredentialRequest -> CredentialError.InvalidCredentialRequest
    OpenIdCredentialOfferError.InvalidEncryptionParameters -> CredentialError.InvalidEncryptionParameters
    OpenIdCredentialOfferError.InvalidNonce -> CredentialError.InvalidNonce
    OpenIdCredentialOfferError.InvalidProof -> CredentialError.InvalidProof
    OpenIdCredentialOfferError.InvalidRequest -> CredentialError.InvalidRequest
    OpenIdCredentialOfferError.InvalidRequestBearerToken -> CredentialError.InvalidRequestBearerToken
    OpenIdCredentialOfferError.InvalidToken -> CredentialError.InvalidToken
    OpenIdCredentialOfferError.UnauthorizedClient -> CredentialError.UnauthorizedClient
    OpenIdCredentialOfferError.UnauthorizedGrantType -> CredentialError.UnauthorizedGrantType
    OpenIdCredentialOfferError.UnknownCredentialConfiguration -> CredentialError.UnknownCredentialConfiguration
    OpenIdCredentialOfferError.UnknownCredentialIdentifier -> CredentialError.UnknownCredentialIdentifier
}

fun GetVerifiableCredentialParamsError.toFetchCredentialError(): FetchCredentialError = when (this) {
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
    is UnsupportedImageFormat -> this
}

fun GenerateOcaDisplaysError.toGenerateCredentialDisplaysError(): GenerateCredentialDisplaysError = when (this) {
    is OcaError.InvalidRootCaptureBase -> InvalidGenerateMetadataClaims
    is OcaError.Unexpected -> Unexpected(cause)
    is OcaError.UnsupportedImageFormat -> UnsupportedImageFormat
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

fun VerifyVcSdJwtSignatureError.toSaveCredentialFromDeferredError(): SaveCredentialFromDeferredError = when (this) {
    VcSdJwtError.DidDocumentDeactivated,
    VcSdJwtError.InvalidJwt,
    is VcSdJwtError.InvalidVcSdJwt -> IntegrityCheckFailed

    VcSdJwtError.IssuerValidationFailed -> UnknownIssuer
    VcSdJwtError.NetworkError -> NetworkError
    is VcSdJwtError.Unexpected -> Unexpected(cause)
}

fun FetchExistingIssuerCredentialInfoError.toUpdateDeferredCredentialError(): FetchAndUpdateDeferredCredentialError = when (this) {
    is NetworkError -> this
    is Unexpected -> this
    is InvalidSignedMetadata -> this
}

fun FetchVcMetadataByFormatError.toSaveCredentialFromDeferredError(): SaveCredentialFromDeferredError = when (this) {
    OcaError.InvalidOca -> InvalidCredentialOffer
    OcaError.InvalidJsonScheme -> InvalidJsonScheme
    OcaError.UnsupportedCredentialFormat -> UnsupportedCredentialFormat
    OcaError.NetworkError -> NetworkError
    is OcaError.Unexpected -> Unexpected(cause)
}

fun GenerateCredentialDisplaysError.toSaveCredentialFromDeferredError(): SaveCredentialFromDeferredError = when (this) {
    is InvalidGenerateMetadataClaims -> this
    is Unexpected -> this
    is UnsupportedImageFormat -> this
}

fun GenerateCredentialDisplaysError.toUpdateDeferredCredentialError(): UpdateDeferredCredentialError = when (this) {
    is InvalidGenerateMetadataClaims -> this
    is Unexpected -> this
    is UnsupportedImageFormat -> this
}

fun GenerateCredentialDisplaysError.toFetchCredentialError(): FetchCredentialError = when (this) {
    is InvalidGenerateMetadataClaims -> this
    is Unexpected -> this
    is UnsupportedImageFormat -> this
}

fun DeferredCredentialRepositoryError.toRefreshDeferredCredentialsError(): RefreshDeferredCredentialsError = when (this) {
    is SsiError.Unexpected -> Unexpected(cause)
}

fun DeferredCredentialRepositoryError.toUpdateDeferredCredentialError(): UpdateDeferredCredentialError = when (this) {
    is SsiError.Unexpected -> Unexpected(cause)
}
fun CredentialOfferRepositoryError.toSaveCredentialFromDeferredError(): SaveCredentialFromDeferredError = when (this) {
    is SsiError.Unexpected -> Unexpected(cause)
}

fun CredentialOfferRepositoryError.toUpdateDeferredCredentialError(): UpdateDeferredCredentialError = when (this) {
    is SsiError.Unexpected -> Unexpected(cause)
}

fun FetchIssuerCredentialInfoError.toFetchExistingIssuerCredentialInfoError(): FetchExistingIssuerCredentialInfoError = when (this) {
    OpenIdCredentialOfferError.NetworkInfoError -> NetworkError
    is OpenIdCredentialOfferError.Unexpected -> Unexpected(cause)
    is OpenIdCredentialOfferError.InvalidSignedMetadata -> InvalidSignedMetadata(message)
}

fun CreateCredentialRequestError.toUpdateDeferredCredentialError(): FetchAndUpdateDeferredCredentialError = when (this) {
    is OpenIdCredentialOfferError.Unexpected -> Unexpected(cause)
}

fun GetPayloadEncryptionTypeError.toFetchCredentialError(): FetchCredentialError = when (this) {
    is PayloadEncryptionError.IncompatibleDeviceProofKeyStorage -> IncompatibleDeviceKeyStorage
    is PayloadEncryptionError.UnsupportedProofKeyStorageSecurityLevel -> UnsupportedKeyStorageSecurityLevel
    is PayloadEncryptionError.Unexpected -> Unexpected(throwable)
}

fun GetPayloadEncryptionTypeError.toUpdateDeferredCredentialError(): FetchAndUpdateDeferredCredentialError = when (this) {
    is PayloadEncryptionError.IncompatibleDeviceProofKeyStorage -> IncompatibleDeviceKeyStorage
    is PayloadEncryptionError.UnsupportedProofKeyStorageSecurityLevel -> UnsupportedKeyStorageSecurityLevel
    is PayloadEncryptionError.Unexpected -> Unexpected(throwable)
}

fun UpdateDeferredCredentialError.toFetchAndUpdateDeferredCredentialError(): FetchAndUpdateDeferredCredentialError = when (this) {
    is InvalidCredentialOffer -> this
    is InvalidGenerateMetadataClaims -> this
    is CredentialError.InvalidIssuerCredentialInfo -> this
    is InvalidSignedMetadata -> this
    is NetworkError -> this
    is Unexpected -> this
    is UnsupportedImageFormat -> this
}

fun FetchAccessTokenError.toUpdateDeferredCredentialError(): UpdateDeferredCredentialError = when (this) {
    OpenIdCredentialOfferError.InvalidClient,
    OpenIdCredentialOfferError.InvalidCredentialOffer,
    OpenIdCredentialOfferError.InvalidGrant,
    OpenIdCredentialOfferError.InvalidRequest,
    OpenIdCredentialOfferError.UnauthorizedClient,
    OpenIdCredentialOfferError.UnauthorizedGrantType -> InvalidCredentialOffer
    OpenIdCredentialOfferError.NetworkInfoError -> NetworkError
    is OpenIdCredentialOfferError.Unexpected -> Unexpected(cause)
}

fun FetchIssuerConfigurationError.toUpdateDeferredCredentialError(): UpdateDeferredCredentialError = when (this) {
    is OpenIdCredentialOfferError.InvalidSignedMetadata -> InvalidSignedMetadata(message)
    OpenIdCredentialOfferError.NetworkInfoError -> NetworkError
    is OpenIdCredentialOfferError.Unexpected -> Unexpected(cause)
}

fun FetchDeferredCredentialError.toUpdateDeferredCredentialError(): UpdateDeferredCredentialError = when (this) {
    OpenIdCredentialOfferError.InsufficientScope,
    OpenIdCredentialOfferError.InvalidClient,
    OpenIdCredentialOfferError.InvalidEncryptionParameters,
    OpenIdCredentialOfferError.InvalidGrant,
    OpenIdCredentialOfferError.InvalidNonce,
    OpenIdCredentialOfferError.InvalidProof,
    OpenIdCredentialOfferError.InvalidRequest,
    OpenIdCredentialOfferError.UnknownCredentialConfiguration,
    OpenIdCredentialOfferError.UnknownCredentialIdentifier,
    OpenIdCredentialOfferError.UnauthorizedClient,
    OpenIdCredentialOfferError.UnauthorizedGrantType,
    OpenIdCredentialOfferError.InvalidCredentialRequest -> Unexpected(null)
    OpenIdCredentialOfferError.InvalidTransactionId,
    OpenIdCredentialOfferError.CredentialRequestDenied,
    OpenIdCredentialOfferError.InvalidCredentialOffer,
    OpenIdCredentialOfferError.InvalidRequestBearerToken,
    OpenIdCredentialOfferError.InvalidToken -> InvalidCredentialOffer
    OpenIdCredentialOfferError.NetworkInfoError -> NetworkError
    is OpenIdCredentialOfferError.Unexpected -> Unexpected(cause)
}

fun CredentialRepositoryError.toFetchExistingIssuerCredentialInfoError(): FetchExistingIssuerCredentialInfoError = when (this) {
    is SsiError.Unexpected -> Unexpected(cause)
}

fun DeleteBundleItemsByAmountError.toFetchCredentialError(): FetchCredentialError = when (this) {
    is DeleteBundleItemsByAmountError.Unexpected -> Unexpected(cause)
}

fun ValidateImageError.toGenerateMetadataDisplaysError(): GenerateMetadataDisplaysError = when (this) {
    ImageValidationError.UnsupportedImageFormat -> UnsupportedImageFormat
}
