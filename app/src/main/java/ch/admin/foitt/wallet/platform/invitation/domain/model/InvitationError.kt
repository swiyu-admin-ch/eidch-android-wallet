package ch.admin.foitt.wallet.platform.invitation.domain.model

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.FetchPresentationRequestError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestError
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CredentialPresentationError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ProcessPresentationRequestError
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
    data class InvalidPresentation(val responseUri: String) : ProcessInvitationError
    data object CredentialOfferExpired : ProcessInvitationError
    data object UnknownIssuer : ProcessInvitationError
    data object UnknownVerifier : ProcessInvitationError
    data object UnsupportedKeyStorageSecurityLevel : ProcessInvitationError
    data object IncompatibleDeviceKeyStorage : ProcessInvitationError
    data class MetadataMisconfiguration(val message: String) : ProcessInvitationError
    data object Unexpected : ProcessInvitationError, GetPresentationRequestError, ValidateInvitationError
}

sealed interface ProcessInvitationError : InvitationError
sealed interface GetCredentialOfferError : InvitationError
sealed interface GetPresentationRequestError : InvitationError
sealed interface ValidateInvitationError : InvitationError

//region Error to Error mappings
internal fun FetchPresentationRequestError.toGetPresentationRequestError(): GetPresentationRequestError = when (this) {
    PresentationRequestError.NetworkError -> InvitationError.NetworkError
    is PresentationRequestError.Unexpected -> InvitationError.InvalidPresentationRequest
}

internal fun GetPresentationRequestError.toValidateInvitationError(): ValidateInvitationError = when (this) {
    is InvitationError.InvalidUri -> this
    is InvitationError.NetworkError -> this
    is InvitationError.InvalidPresentationRequest -> this
    is InvitationError.Unexpected -> this
}

internal fun GetCredentialOfferError.toValidateInvitationError(): ValidateInvitationError = when (this) {
    is InvitationError.CredentialOfferDeserializationFailed -> this
    is InvitationError.NoCredentialsFound -> this
    is InvitationError.UnsupportedGrantType -> this
}

internal fun FetchCredentialError.toProcessInvitationError(): ProcessInvitationError = when (this) {
    CredentialError.InvalidGrant -> InvitationError.CredentialOfferExpired
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
    CredentialError.InvalidGenerateMetadataClaims -> InvitationError.InvalidCredentialOffer
    CredentialError.NetworkError -> InvitationError.NetworkError
    CredentialError.DatabaseError,
    is CredentialError.Unexpected -> InvitationError.Unexpected
    CredentialError.UnknownIssuer -> InvitationError.UnknownIssuer
    CredentialError.UnsupportedKeyStorageSecurityLevel -> InvitationError.UnsupportedKeyStorageSecurityLevel
    CredentialError.IncompatibleDeviceKeyStorage -> InvitationError.IncompatibleDeviceKeyStorage
    is CredentialError.MetadataMisconfiguration -> InvitationError.MetadataMisconfiguration(message)
}

internal fun ValidateInvitationError.toProcessInvitationError(): ProcessInvitationError = when (this) {
    is InvitationError.InvalidUri,
    is InvitationError.UnknownSchema -> InvitationError.InvalidInput
    is InvitationError.InvalidPresentationRequest -> this
    is InvitationError.NetworkError -> this
    is InvitationError.UnsupportedGrantType,
    is InvitationError.CredentialOfferDeserializationFailed,
    is InvitationError.NoCredentialsFound -> InvitationError.InvalidCredentialOffer
    is InvitationError.Unexpected -> this
}

internal fun Throwable.toGetCredentialOfferError(message: String): GetCredentialOfferError {
    Timber.e(t = this, message = message)
    return InvitationError.CredentialOfferDeserializationFailed(this)
}

internal fun JsonParsingError.toGetCredentialOfferError(): GetCredentialOfferError = when (this) {
    is JsonError.Unexpected -> InvitationError.CredentialOfferDeserializationFailed(throwable)
}

internal fun ProcessPresentationRequestError.toProcessInvitationError(): ProcessInvitationError = when (this) {
    is CredentialPresentationError.EmptyWallet -> InvitationError.EmptyWallet(responseUri)
    is CredentialPresentationError.NoCompatibleCredential -> InvitationError.NoCompatibleCredential(responseUri)
    is CredentialPresentationError.InvalidPresentation -> InvitationError.InvalidPresentation(responseUri)
    is CredentialPresentationError.Unexpected -> InvitationError.Unexpected
    is CredentialPresentationError.UnknownVerifier -> InvitationError.UnknownVerifier
    CredentialPresentationError.NetworkError -> InvitationError.NetworkError
}

internal fun ProcessInvitationError.toErrorDisplay(): InvitationErrorScreenState = when (this) {
    InvitationError.NetworkError -> InvitationErrorScreenState.NETWORK_ERROR
    InvitationError.InvalidCredentialOffer,
    InvitationError.InvalidInput,
    is InvitationError.MetadataMisconfiguration,
    InvitationError.CredentialOfferExpired -> InvitationErrorScreenState.INVALID_CREDENTIAL
    InvitationError.InvalidPresentationRequest,
    is InvitationError.InvalidPresentation -> InvitationErrorScreenState.INVALID_PRESENTATION
    is InvitationError.EmptyWallet -> InvitationErrorScreenState.EMPTY_WALLET
    is InvitationError.NoCompatibleCredential -> InvitationErrorScreenState.NO_COMPATIBLE_CREDENTIAL
    InvitationError.UnknownVerifier,
    InvitationError.Unexpected -> {
        Timber.w("Unexpected state on processing deeplink")
        InvitationErrorScreenState.UNEXPECTED
    }
    InvitationError.UnknownIssuer -> InvitationErrorScreenState.UNKNOWN_ISSUER
    InvitationError.UnsupportedKeyStorageSecurityLevel -> InvitationErrorScreenState.UNSUPPORTED_KEY_STORAGE
    InvitationError.IncompatibleDeviceKeyStorage -> InvitationErrorScreenState.UNSUPPORTED_KEY_STORAGE_CAPABILITIES
}

internal fun Throwable.toGetPresentationRequestError(uri: URI): GetPresentationRequestError {
    // do not log this to dynatrace
    Timber.d("Invalid uri: $uri")
    return InvitationError.InvalidUri
}
//endregion
