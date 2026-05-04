@file:Suppress("TooManyFunctions")

package ch.admin.foitt.wallet.platform.invitation.domain.model

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.FetchPresentationRequestError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestError
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CredentialPresentationError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ProcessPresentationRequestError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ValidatePresentationRequestError
import ch.admin.foitt.wallet.platform.genericScreens.domain.model.GenericErrorScreenState
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.CredentialOfferDeserializationFailed
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.CredentialOfferExpired
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.CredentialRequestDenied
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.EmptyWallet
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.IncompatibleDeviceKeyStorage
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.InsufficientScope
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.InvalidClient
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.InvalidCredentialOffer
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.InvalidCredentialRequest
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.InvalidEncryptionParameters
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.InvalidInput
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.InvalidNonce
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.InvalidPresentation
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.InvalidPresentationRequest
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.InvalidProof
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.InvalidRequest
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.InvalidRequestBearerToken
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.InvalidToken
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.InvalidUri
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.MetadataMisconfiguration
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.NetworkError
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.NoCompatibleCredential
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.NoCredentialsFound
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.UnauthorizedClient
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.UnauthorizedGrantType
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.Unexpected
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.UnknownCredentialConfiguration
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.UnknownCredentialIdentifier
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.UnknownIssuer
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.UnknownSchema
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.UnknownVerifier
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.UnsupportedGrantType
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.UnsupportedKeyStorageSecurityLevel
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination.InvitationFailureScreen
import ch.admin.foitt.wallet.platform.utils.JsonError
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import timber.log.Timber
import java.net.URI

interface InvitationError {
    data object UnknownSchema : ValidateInvitationError
    data object InvalidUri : GetPresentationRequestError, ValidateInvitationError
    data object InvalidCredentialOffer : ProcessInvitationError
    data object NoCredentialsFound : GetCredentialOfferError, ValidateInvitationError
    data class UnsupportedGrantType(val message: String) : GetCredentialOfferError, ValidateInvitationError
    data class CredentialOfferDeserializationFailed(val throwable: Throwable) : GetCredentialOfferError, ValidateInvitationError
    data object NetworkError : ProcessInvitationError, GetPresentationRequestError, ValidateInvitationError
    data class EmptyWallet(val responseUri: String? = null) : ProcessInvitationError
    data class NoCompatibleCredential(val responseUri: String? = null) : ProcessInvitationError
    data object InvalidInput : ProcessInvitationError
    data object InvalidPresentationRequest : GetPresentationRequestError, ValidateInvitationError, ProcessInvitationError
    data class InvalidPresentation(val responseUri: String) : ProcessInvitationError, GetPresentationRequestError, ValidateInvitationError
    data object CredentialOfferExpired : ProcessInvitationError
    data object UnknownIssuer : ProcessInvitationError
    data object UnknownVerifier : ProcessInvitationError, GetPresentationRequestError, ValidateInvitationError
    data object UnsupportedKeyStorageSecurityLevel : ProcessInvitationError
    data object IncompatibleDeviceKeyStorage : ProcessInvitationError
    data class MetadataMisconfiguration(val message: String) : ProcessInvitationError

    data object CredentialRequestDenied : ProcessInvitationError
    data object InsufficientScope : ProcessInvitationError
    data object InvalidClient : ProcessInvitationError
    data object InvalidCredentialRequest : ProcessInvitationError
    data object InvalidEncryptionParameters : ProcessInvitationError
    data object InvalidNonce : ProcessInvitationError
    data object InvalidProof : ProcessInvitationError
    data object InvalidRequest : ProcessInvitationError
    data object InvalidRequestBearerToken : ProcessInvitationError
    data object InvalidToken : ProcessInvitationError
    data object UnauthorizedClient : ProcessInvitationError
    data object UnauthorizedGrantType : ProcessInvitationError
    data object UnknownCredentialConfiguration : ProcessInvitationError
    data object UnknownCredentialIdentifier : ProcessInvitationError

    data object Unexpected : ProcessInvitationError, GetPresentationRequestError, ValidateInvitationError
}

sealed interface ProcessInvitationError : InvitationError
sealed interface GetCredentialOfferError : InvitationError
sealed interface GetPresentationRequestError : InvitationError
sealed interface ValidateInvitationError : InvitationError

//region Error to Error mappings
internal fun FetchPresentationRequestError.toGetPresentationRequestError(): GetPresentationRequestError = when (this) {
    PresentationRequestError.NetworkError -> NetworkError
    is PresentationRequestError.Unexpected -> InvalidPresentationRequest
}

internal fun GetPresentationRequestError.toValidateInvitationError(): ValidateInvitationError = when (this) {
    is InvalidUri -> this
    is NetworkError -> this
    is InvalidPresentationRequest -> this
    is Unexpected -> this
    is InvalidPresentation -> this
    is UnknownVerifier -> this
}

internal fun GetCredentialOfferError.toValidateInvitationError(): ValidateInvitationError = when (this) {
    is CredentialOfferDeserializationFailed -> this
    is NoCredentialsFound -> this
    is UnsupportedGrantType -> this
}

@Suppress("CyclomaticComplexMethod")
internal fun FetchCredentialError.toProcessInvitationError(): ProcessInvitationError = when (this) {
    CredentialError.InvalidGrant -> CredentialOfferExpired
    CredentialError.IntegrityCheckFailed,
    CredentialError.UnsupportedGrantType,
    CredentialError.InvalidCredentialOffer,
    CredentialError.UnsupportedCredentialFormat,
    CredentialError.UnsupportedCredentialIdentifier,
    CredentialError.UnsupportedProofType,
    CredentialError.UnsupportedCryptographicSuite,
    CredentialError.CredentialParsingError,
    CredentialError.InvalidJsonScheme,
    is CredentialError.InvalidSignedMetadata,
    CredentialError.InvalidIssuerCredentialInfo,
    CredentialError.UnsupportedImageFormat,
    CredentialError.InvalidGenerateMetadataClaims -> InvalidCredentialOffer
    CredentialError.NetworkError -> NetworkError
    CredentialError.DatabaseError,
    is CredentialError.Unexpected -> Unexpected
    CredentialError.UnknownIssuer -> UnknownIssuer
    CredentialError.UnsupportedKeyStorageSecurityLevel -> UnsupportedKeyStorageSecurityLevel
    CredentialError.IncompatibleDeviceKeyStorage -> IncompatibleDeviceKeyStorage
    is CredentialError.MetadataMisconfiguration -> MetadataMisconfiguration(message)
    CredentialError.CredentialRequestDenied -> CredentialRequestDenied
    CredentialError.InsufficientScope -> InsufficientScope
    CredentialError.InvalidClient -> InvalidClient
    CredentialError.InvalidCredentialRequest -> InvalidCredentialRequest
    CredentialError.InvalidEncryptionParameters -> InvalidEncryptionParameters
    CredentialError.InvalidNonce -> InvalidNonce
    CredentialError.InvalidProof -> InvalidProof
    CredentialError.InvalidRequest -> InvalidRequest
    CredentialError.InvalidRequestBearerToken -> InvalidRequestBearerToken
    CredentialError.InvalidToken -> InvalidToken
    CredentialError.UnauthorizedClient -> UnauthorizedClient
    CredentialError.UnauthorizedGrantType -> UnauthorizedGrantType
    CredentialError.UnknownCredentialConfiguration -> UnknownCredentialConfiguration
    CredentialError.UnknownCredentialIdentifier -> UnknownCredentialIdentifier
}

internal fun ValidateInvitationError.toProcessInvitationError(): ProcessInvitationError = when (this) {
    is InvalidUri,
    is UnknownSchema -> InvalidInput
    is InvalidPresentationRequest -> this
    is NetworkError -> this
    is UnsupportedGrantType,
    is CredentialOfferDeserializationFailed,
    is NoCredentialsFound -> InvalidCredentialOffer
    is Unexpected -> this
    is InvalidPresentation -> this
    is UnknownVerifier -> this
}

internal fun Throwable.toGetCredentialOfferError(message: String): GetCredentialOfferError {
    Timber.e(t = this, message = message)
    return CredentialOfferDeserializationFailed(this)
}

internal fun JsonParsingError.toGetCredentialOfferError(): GetCredentialOfferError = when (this) {
    is JsonError.Unexpected -> CredentialOfferDeserializationFailed(throwable)
}

internal fun ProcessPresentationRequestError.toProcessInvitationError(): ProcessInvitationError = when (this) {
    is CredentialPresentationError.EmptyWallet -> EmptyWallet(responseUri)
    is CredentialPresentationError.NoCompatibleCredential -> NoCompatibleCredential(responseUri)
    is CredentialPresentationError.InvalidPresentation -> InvalidPresentation(responseUri)
    is CredentialPresentationError.Unexpected -> Unexpected
    is CredentialPresentationError.UnknownVerifier -> UnknownVerifier
    CredentialPresentationError.NetworkError -> NetworkError
}

@Suppress("CyclomaticComplexMethod")
internal fun ProcessInvitationError.toErrorDestination(uri: String?): Destination = when (this) {
    NetworkError -> InvitationFailureScreen(invitationError = InvitationErrorScreenState.NETWORK_ERROR, uri = uri)
    InvalidCredentialOffer,
    InvalidInput,
    is MetadataMisconfiguration,
    CredentialOfferExpired -> InvitationFailureScreen(invitationError = InvitationErrorScreenState.INVALID_CREDENTIAL, uri = uri)
    InvalidPresentationRequest,
    is InvalidPresentation -> InvitationFailureScreen(invitationError = InvitationErrorScreenState.INVALID_PRESENTATION, uri = uri)
    is EmptyWallet -> InvitationFailureScreen(invitationError = InvitationErrorScreenState.EMPTY_WALLET, uri = uri)
    is NoCompatibleCredential -> InvitationFailureScreen(invitationError = InvitationErrorScreenState.NO_COMPATIBLE_CREDENTIAL, uri = uri)
    UnknownVerifier,
    Unexpected -> {
        Timber.w("Unexpected state on processing deeplink")
        InvitationFailureScreen(invitationError = InvitationErrorScreenState.UNEXPECTED, uri = uri)
    }
    UnknownIssuer -> InvitationFailureScreen(invitationError = InvitationErrorScreenState.UNKNOWN_ISSUER, uri = uri)
    UnsupportedKeyStorageSecurityLevel -> InvitationFailureScreen(
        invitationError = InvitationErrorScreenState.UNSUPPORTED_KEY_STORAGE,
        uri = uri
    )
    IncompatibleDeviceKeyStorage -> InvitationFailureScreen(
        invitationError = InvitationErrorScreenState.UNSUPPORTED_KEY_STORAGE_CAPABILITIES,
        uri = uri
    )
    CredentialRequestDenied -> Destination.GenericErrorScreen(error = GenericErrorScreenState.CREDENTIAL_REQUEST_DENIED)
    InsufficientScope -> Destination.GenericErrorScreen(error = GenericErrorScreenState.INSUFFICIENT_SCOPE)
    InvalidClient -> Destination.GenericErrorScreen(error = GenericErrorScreenState.INVALID_CLIENT)
    InvalidCredentialRequest -> Destination.GenericErrorScreen(error = GenericErrorScreenState.INVALID_CREDENTIAL_REQUEST)
    InvalidEncryptionParameters -> Destination.GenericErrorScreen(
        error = GenericErrorScreenState.INVALID_ENCRYPTION_PARAMETERS
    )
    InvalidNonce -> Destination.GenericErrorScreen(error = GenericErrorScreenState.INVALID_NONCE)
    InvalidProof -> Destination.GenericErrorScreen(error = GenericErrorScreenState.INVALID_PROOF)
    InvalidRequest -> Destination.GenericErrorScreen(error = GenericErrorScreenState.INVALID_REQUEST)
    InvalidRequestBearerToken -> Destination.GenericErrorScreen(error = GenericErrorScreenState.INVALID_REQUEST_BEARER_TOKEN)
    InvalidToken -> Destination.GenericErrorScreen(error = GenericErrorScreenState.INVALID_TOKEN)
    UnauthorizedClient -> Destination.GenericErrorScreen(error = GenericErrorScreenState.UNAUTHORIZED_CLIENT)
    UnauthorizedGrantType -> Destination.GenericErrorScreen(error = GenericErrorScreenState.UNAUTHORIZED_GRANT_TYPE)
    UnknownCredentialConfiguration -> Destination.GenericErrorScreen(
        error = GenericErrorScreenState.UNKNOWN_CREDENTIAL_CONFIGURATION
    )
    UnknownCredentialIdentifier -> Destination.GenericErrorScreen(
        error = GenericErrorScreenState.UNKNOWN_CREDENTIAL_IDENTIFIER
    )
}

internal fun Throwable.toGetPresentationRequestError(uri: URI): GetPresentationRequestError {
    // do not log this to dynatrace
    Timber.d("Invalid uri: $uri")
    return InvalidUri
}

internal fun ValidatePresentationRequestError.toGetPresentationRequestError(): GetPresentationRequestError = when (this) {
    is CredentialPresentationError.InvalidPresentation -> InvalidPresentation(responseUri)
    is CredentialPresentationError.UnknownVerifier -> UnknownVerifier
    is CredentialPresentationError.NetworkError -> NetworkError
    is CredentialPresentationError.Unexpected -> Unexpected
}
//endregion
