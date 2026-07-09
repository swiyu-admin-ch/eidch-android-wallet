@file:Suppress("TooManyFunctions")

package ch.admin.foitt.wallet.platform.pushNotification.domain.model

import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AttestationError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.GenerateProofOfPossessionError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.RequestClientAttestationError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.utils.JsonError
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import timber.log.Timber
import java.io.IOException

sealed interface PushNotificationError {
    data object InvalidFormat :
        RegisterPushDeviceTokenError,
        UpdatePushDeviceTokenError

    data object InvalidClientAttestation :
        RegisterPushDeviceTokenError,
        UpdatePushDeviceTokenError,
        DeletePushIdError,
        GeneratePushClientAttestationError

    data object NetworkError :
        FetchPushChallengeError,
        RegisterPushDeviceTokenError,
        UpdatePushDeviceTokenError,
        DeletePushIdError,
        GeneratePushClientAttestationError

    data class Unexpected(val cause: Throwable?) :
        FetchPushDeviceTokenError,
        FetchPushChallengeError,
        RegisterPushDeviceTokenError,
        UpdatePushDeviceTokenError,
        DeletePushIdError,
        GeneratePushClientAttestationError
}

sealed interface FetchPushDeviceTokenError
sealed interface FetchPushChallengeError
sealed interface RegisterPushDeviceTokenError
sealed interface UpdatePushDeviceTokenError
sealed interface DeletePushIdError
sealed interface GeneratePushClientAttestationError

internal fun Throwable.toFetchPushDeviceTokenError(message: String): FetchPushDeviceTokenError {
    Timber.e(t = this, message = message)
    return PushNotificationError.Unexpected(this)
}

internal fun Throwable.toFetchPushChallengeError(message: String): FetchPushChallengeError {
    Timber.e(t = this, message = message)
    return when (this) {
        is IOException -> PushNotificationError.NetworkError
        else -> PushNotificationError.Unexpected(this)
    }
}

internal fun Throwable.toRegisterPushDeviceTokenError(message: String): RegisterPushDeviceTokenError {
    Timber.e(t = this, message = message)
    return when (this) {
        is ClientRequestException -> this.toRegisterPushDeviceTokenError()
        is IOException -> PushNotificationError.NetworkError
        else -> PushNotificationError.Unexpected(this)
    }
}

private fun ClientRequestException.toRegisterPushDeviceTokenError(): RegisterPushDeviceTokenError {
    return when (this.response.status) {
        HttpStatusCode.BadRequest -> PushNotificationError.InvalidFormat
        HttpStatusCode.Unauthorized -> PushNotificationError.InvalidClientAttestation
        else -> PushNotificationError.Unexpected(this.cause)
    }
}

internal fun Throwable.toUpdatePushDeviceTokenError(message: String): UpdatePushDeviceTokenError {
    Timber.e(t = this, message = message)
    return when (this) {
        is ClientRequestException -> this.toUpdatePushDeviceTokenError()
        is IOException -> PushNotificationError.NetworkError
        else -> PushNotificationError.Unexpected(this)
    }
}

private fun ClientRequestException.toUpdatePushDeviceTokenError(): UpdatePushDeviceTokenError {
    return when (this.response.status) {
        HttpStatusCode.BadRequest -> PushNotificationError.InvalidFormat
        HttpStatusCode.Unauthorized -> PushNotificationError.InvalidClientAttestation
        else -> PushNotificationError.Unexpected(this.cause)
    }
}

internal fun Throwable.toDeletePushIdError(message: String): DeletePushIdError {
    Timber.e(t = this, message = message)
    return when (this) {
        is ClientRequestException -> this.toDeletePushIdError()
        is IOException -> PushNotificationError.NetworkError
        else -> PushNotificationError.Unexpected(this)
    }
}

private fun ClientRequestException.toDeletePushIdError(): DeletePushIdError {
    return when (this.response.status) {
        HttpStatusCode.Unauthorized -> PushNotificationError.InvalidClientAttestation
        else -> PushNotificationError.Unexpected(this.cause)
    }
}

internal fun FetchPushChallengeError.toGeneratePushClientAttestationError(): GeneratePushClientAttestationError = when (this) {
    PushNotificationError.NetworkError -> PushNotificationError.NetworkError
    is PushNotificationError.Unexpected -> PushNotificationError.Unexpected(cause)
}

internal fun FetchPushDeviceTokenError.toUpdatePushDeviceTokenError(): UpdatePushDeviceTokenError = when (this) {
    is PushNotificationError.Unexpected -> PushNotificationError.Unexpected(cause)
}

internal fun EIdRequestCaseRepositoryError.toUpdatePushDeviceTokenError(): UpdatePushDeviceTokenError = when (this) {
    is EIdRequestError.Unexpected -> PushNotificationError.Unexpected(cause)
}

internal fun JsonParsingError.toUpdatePushDeviceTokenError(): UpdatePushDeviceTokenError = when (this) {
    is JsonError.Unexpected -> PushNotificationError.Unexpected(throwable)
}

internal fun GeneratePushClientAttestationError.toUpdatePushDeviceTokenError(): UpdatePushDeviceTokenError = when (this) {
    PushNotificationError.InvalidClientAttestation -> PushNotificationError.InvalidClientAttestation
    PushNotificationError.NetworkError -> PushNotificationError.NetworkError
    is PushNotificationError.Unexpected -> PushNotificationError.Unexpected(cause)
}

internal fun GeneratePushClientAttestationError.toDeletePushIdError(): DeletePushIdError = when (this) {
    PushNotificationError.InvalidClientAttestation -> PushNotificationError.InvalidClientAttestation
    PushNotificationError.NetworkError -> PushNotificationError.NetworkError
    is PushNotificationError.Unexpected -> PushNotificationError.Unexpected(cause)
}

// External types

internal fun RequestClientAttestationError.toGeneratePushClientAttestationError(): GeneratePushClientAttestationError = when (this) {
    is AttestationError.ValidationError -> PushNotificationError.InvalidClientAttestation
    AttestationError.NetworkError -> PushNotificationError.NetworkError
    AttestationError.SocketTimeoutError -> PushNotificationError.Unexpected(null)
    is AttestationError.Unexpected -> PushNotificationError.Unexpected(throwable)
}

internal fun GenerateProofOfPossessionError.toGeneratePushClientAttestationError(): GeneratePushClientAttestationError = when (this) {
    is AttestationError.Unexpected -> PushNotificationError.Unexpected(throwable)
}
