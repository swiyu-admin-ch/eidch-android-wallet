@file:Suppress("TooManyFunctions")

package ch.admin.foitt.wallet.platform.appAttestation.domain.model

import ch.admin.foitt.openid4vc.domain.model.CreateJwkError
import ch.admin.foitt.openid4vc.domain.model.GetKeyPairError
import ch.admin.foitt.openid4vc.domain.model.JwkError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AttestationError.NetworkError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AttestationError.Unexpected
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AttestationError.ValidationError
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.model.CreateJWSKeyPairError
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.model.KeyPairError
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
        AppIntegrityRepositoryError,
        RequestKeyAttestationError
    data class Unexpected(val throwable: Throwable?) :
        RequestClientAttestationError,
        AppAttestationRepositoryError,
        ClientAttestationRepositoryError,
        ValidateClientAttestationError,
        AppIntegrityRepositoryError,
        RequestKeyAttestationError,
        ValidateKeyAttestationError,
        GenerateProofOfPossessionError
}

sealed interface AppAttestationRepositoryError
sealed interface AppIntegrityRepositoryError
sealed interface ClientAttestationRepositoryError
sealed interface RequestClientAttestationError
sealed interface ValidateClientAttestationError
sealed interface RequestKeyAttestationError
sealed interface ValidateKeyAttestationError
sealed interface GenerateProofOfPossessionError

internal fun Throwable.toAppAttestationRepositoryError(message: String): AppAttestationRepositoryError {
    Timber.e(t = this, message = message)
    return when (this) {
        is IOException -> NetworkError
        else -> Unexpected(this)
    }
}

internal fun Throwable.toAppIntegrityRepositoryError(message: String): AppIntegrityRepositoryError {
    Timber.e(t = this, message = message)
    return when (this) {
        is IOException -> NetworkError
        else -> Unexpected(this)
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
    is NetworkError -> this
    is Unexpected -> this
}

internal fun AppIntegrityRepositoryError.toRequestClientAttestationError(): RequestClientAttestationError = when (this) {
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
    is NetworkError -> this
    is Unexpected -> this
}

internal fun CreateJWSKeyPairError.toRequestKeyAttestationError(): RequestKeyAttestationError = when (this) {
    is KeyPairError.IncompatibleDeviceProofKeyStorage -> AttestationError.IncompatibleDeviceKeyStorage
    is KeyPairError.UnsupportedProofKeyStorageSecurityLevel -> AttestationError.UnsupportedKeyStorageSecurityLevel
    is KeyPairError.Unexpected -> Unexpected(throwable)
}

internal fun CreateJwkError.toRequestKeyAttestationError(): RequestKeyAttestationError = when (this) {
    is JwkError.Unexpected -> Unexpected(cause)
    is JwkError.UnsupportedCryptographicSuite -> Unexpected(Exception("Unsupported cryptographic key"))
}
