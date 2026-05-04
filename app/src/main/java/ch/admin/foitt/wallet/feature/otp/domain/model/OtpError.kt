package ch.admin.foitt.wallet.feature.otp.domain.model

import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AppAttestationRepositoryError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AttestationError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.GenerateProofOfPossessionError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.RequestClientAttestationError
import ch.admin.foitt.wallet.platform.utils.JsonError
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import timber.log.Timber
import java.io.IOException

interface OtpError {
    data object InvalidFormat : RequestOtpError
    data object InvalidField : RequestOtpError
    data object ServiceDeactivated : RequestOtpError
    data object InvalidClientAttestation : RequestOtpError
    data object OtpExpired : RequestOtpError
    data object TooManyRequests : RequestOtpError
    data object NetworkError : RequestOtpError
    data class Unexpected(val throwable: Throwable?) : RequestOtpError
}

sealed interface RequestOtpError

internal suspend fun Throwable.toRequestOtpError(message: String): RequestOtpError {
    Timber.e(t = this, message = message)
    return when (this) {
        is ClientRequestException -> this.toRequestOtpError()
        is IOException -> OtpError.NetworkError
        else -> OtpError.Unexpected(this)
    }
}

private fun ClientRequestException.toRequestOtpError(): RequestOtpError {
    return when (this.response.status) {
        HttpStatusCode.BadRequest -> OtpError.InvalidFormat
        HttpStatusCode.Forbidden -> OtpError.InvalidField
        HttpStatusCode.TooManyRequests -> OtpError.TooManyRequests
        HttpStatusCode.Gone -> OtpError.OtpExpired
        HttpStatusCode.fromValue(418) -> OtpError.ServiceDeactivated
        else -> OtpError.Unexpected(this.cause)
    }
}

internal fun RequestClientAttestationError.toRequestOtpError(): RequestOtpError = when (this) {
    is AttestationError.NetworkError -> OtpError.NetworkError
    is AttestationError.Unexpected -> OtpError.Unexpected(throwable)
    is AttestationError.ValidationError -> OtpError.InvalidClientAttestation
}

internal fun AppAttestationRepositoryError.toRequestOtpError(): RequestOtpError = when (this) {
    AttestationError.NetworkError -> OtpError.NetworkError
    is AttestationError.Unexpected -> OtpError.Unexpected(throwable)
}

internal fun JsonParsingError.toRequestOtpError(): RequestOtpError = when (this) {
    is JsonError.Unexpected -> OtpError.Unexpected(this.throwable)
}

internal fun GenerateProofOfPossessionError.toRequestOtpError(): RequestOtpError = when (this) {
    is AttestationError.Unexpected -> OtpError.Unexpected(this.throwable)
}
