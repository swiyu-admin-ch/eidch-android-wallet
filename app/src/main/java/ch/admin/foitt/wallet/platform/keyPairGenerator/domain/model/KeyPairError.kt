package ch.admin.foitt.wallet.platform.keyPairGenerator.domain.model

import timber.log.Timber

interface KeyPairError {
    data object UnsupportedProofKeyStorageSecurityLevel : CreateJWSKeyPairError
    data object IncompatibleDeviceProofKeyStorage : CreateJWSKeyPairError
    data class Unexpected(val throwable: Throwable?) : CreateJWSKeyPairError, CreateKeyGenSpecError
}

sealed interface CreateJWSKeyPairError
sealed interface CreateKeyGenSpecError

internal fun Throwable.toCreateKeyGenSpecError(message: String): CreateKeyGenSpecError {
    Timber.e(t = this, message = message)
    return KeyPairError.Unexpected(this)
}

fun CreateKeyGenSpecError.toCreateJWSKeyPairError(): CreateJWSKeyPairError = when (this) {
    is KeyPairError.Unexpected -> this
}

fun Throwable.toCreateJWSKeyPairError(message: String): CreateJWSKeyPairError {
    Timber.e(this, message)
    return KeyPairError.Unexpected(this)
}
