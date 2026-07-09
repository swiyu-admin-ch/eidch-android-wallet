@file:Suppress("TooManyFunctions")

package ch.admin.foitt.wallet.platform.appAttestation.domain.model

import ch.admin.foitt.openid4vc.domain.model.CreateJwkError
import ch.admin.foitt.openid4vc.domain.model.GenerateDPoPKeyPairError
import ch.admin.foitt.openid4vc.domain.model.GetKeyPairError
import ch.admin.foitt.openid4vc.domain.model.JwkError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AttestationError.IncompatibleDeviceKeyStorage
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AttestationError.NetworkError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AttestationError.SocketTimeoutError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AttestationError.Unexpected
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AttestationError.UnsupportedKeyStorageSecurityLevel
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AttestationError.ValidationError
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.model.CreateJWSKeyPairError
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.model.KeyPairError
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import timber.log.Timber
import java.io.IOException
import ch.admin.foitt.openid4vc.domain.model.KeyPairError as OIDKeyPairError

interface AttestationError {
    data object IncompatibleDeviceKeyStorage : RequestKeyAttestationError

    data object UnsupportedKeyStorageSecurityLevel : RequestKeyAttestationError

    data class ValidationError(val message: String?) :
        RequestClientAttestationError,
        ValidateClientAttestationError,
        RequestKeyAttestationError,
        ValidateKeyAttestationError

    data object NetworkError :
        RequestClientAttestationError,
        AppAttestationRepositoryError,
        RequestKeyAttestationError

    data object SocketTimeoutError :
        AppAttestationRepositoryError,
        RequestKeyAttestationError,
        RequestClientAttestationError

    data class Unexpected(val throwable: Throwable?) :
        RequestClientAttestationError,
        AppAttestationRepositoryError,
        ClientAttestationRepositoryError,
        ValidateClientAttestationError,
        RequestKeyAttestationError,
        ValidateKeyAttestationError,
        GenerateProofOfPossessionError
}

sealed interface AppAttestationRepositoryError
sealed interface ClientAttestationRepositoryError
sealed interface RequestClientAttestationError
sealed interface ValidateClientAttestationError
sealed interface RequestKeyAttestationError
sealed interface ValidateKeyAttestationError
sealed interface GenerateProofOfPossessionError

internal fun Throwable.toAppAttestationRepositoryError(message: String): AppAttestationRepositoryError {
    Timber.e(t = this, message = message)
    return when (this) {
        is ClientRequestException -> this.toAppAttestationRepositoryError()
        is SocketTimeoutException -> SocketTimeoutError
        is IOException -> NetworkError
        else -> Unexpected(this)
    }
}

private fun ClientRequestException.toAppAttestationRepositoryError(): AppAttestationRepositoryError {
    return when (this.response.status) {
        HttpStatusCode(418, "Temporary Deactivated") -> SocketTimeoutError
        else -> Unexpected(this.cause)
    }
}

internal fun Throwable.toClientAttestationRepositoryError(message: String): ClientAttestationRepositoryError {
    Timber.e(t = this, message = message)
    return when (this) {
        else -> Unexpected(this)
    }
}

internal fun Throwable.toRequestClientAttestationError(message: String): RequestClientAttestationError {
    Timber.e(t = this, message = message)
    return Unexpected(this)
}

internal fun Throwable.toRequestKeyAttestationError(message: String): RequestKeyAttestationError {
    Timber.e(t = this, message = message)
    return Unexpected(this)
}

internal fun Throwable.toGenerateProofOfPossessionError(message: String): GenerateProofOfPossessionError {
    Timber.e(t = this, message = message)
    return Unexpected(this)
}

internal fun GetKeyPairError.toGenerateProofOfPossessionError(): GenerateProofOfPossessionError = when (this) {
    OIDKeyPairError.NotFound -> Unexpected(IllegalStateException("KeyPair not found"))
    is OIDKeyPairError.Unexpected -> Unexpected(throwable)
}

internal fun ValidateClientAttestationError.toRequestClientAttestationError(): RequestClientAttestationError = when (this) {
    is ValidationError -> this
    is Unexpected -> this
}

internal fun AppAttestationRepositoryError.toRequestClientAttestationError(): RequestClientAttestationError = when (this) {
    is SocketTimeoutError -> this
    is NetworkError -> this
    is Unexpected -> this
}

internal fun ClientAttestationRepositoryError.toRequestClientAttestationError(): RequestClientAttestationError = when (this) {
    is Unexpected -> this
}

internal fun CreateJWSKeyPairError.toRequestClientAttestationError(): RequestClientAttestationError = when (this) {
    is KeyPairError.IncompatibleDeviceProofKeyStorage,
    is KeyPairError.UnsupportedProofKeyStorageSecurityLevel -> Unexpected(null)
    is KeyPairError.Unexpected -> Unexpected(throwable)
}

internal fun CreateJwkError.toRequestClientAttestationError(): RequestClientAttestationError = when (this) {
    is JwkError.Unexpected -> Unexpected(cause)
    is JwkError.UnsupportedCryptographicSuite -> Unexpected(Exception("Unsupported cryptographic key"))
}

internal fun ValidateKeyAttestationError.toRequestKeyAttestationError(): RequestKeyAttestationError = when (this) {
    is Unexpected -> this
    is ValidationError -> this
}

internal fun AppAttestationRepositoryError.toRequestKeyAttestationError(): RequestKeyAttestationError = when (this) {
    is SocketTimeoutError -> this
    is NetworkError -> this
    is Unexpected -> this
}

internal fun CreateJWSKeyPairError.toRequestKeyAttestationError(): RequestKeyAttestationError = when (this) {
    is KeyPairError.IncompatibleDeviceProofKeyStorage -> IncompatibleDeviceKeyStorage
    is KeyPairError.UnsupportedProofKeyStorageSecurityLevel -> UnsupportedKeyStorageSecurityLevel
    is KeyPairError.Unexpected -> Unexpected(throwable)
}

internal fun CreateJwkError.toRequestKeyAttestationError(): RequestKeyAttestationError = when (this) {
    is JwkError.Unexpected -> Unexpected(cause)
    is JwkError.UnsupportedCryptographicSuite -> Unexpected(Exception("Unsupported cryptographic key"))
}

internal fun RequestKeyAttestationError.toGenerateDPoPKeyPairError(): GenerateDPoPKeyPairError = when (this) {
    is UnsupportedKeyStorageSecurityLevel -> GenerateDPoPKeyPairError.UnsupportedKeyStorageSecurityLevel
    is IncompatibleDeviceKeyStorage -> GenerateDPoPKeyPairError.IncompatibleDeviceProofKeyStorage
    is ValidationError -> GenerateDPoPKeyPairError.Unexpected(IllegalStateException(message))
    is NetworkError -> GenerateDPoPKeyPairError.NetworkError
    is SocketTimeoutError -> GenerateDPoPKeyPairError.Unexpected(null)
    is Unexpected -> GenerateDPoPKeyPairError.Unexpected(throwable)
}
