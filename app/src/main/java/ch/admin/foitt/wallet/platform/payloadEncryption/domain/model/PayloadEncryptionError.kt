package ch.admin.foitt.wallet.platform.payloadEncryption.domain.model

import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.model.CreateJWSKeyPairError
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.model.KeyPairError

sealed interface PayloadEncryptionError {
    data object UnsupportedProofKeyStorageSecurityLevel :
        CreatePayloadEncryptionKeyPairError,
        GetPayloadEncryptionTypeError

    data object IncompatibleDeviceProofKeyStorage :
        CreatePayloadEncryptionKeyPairError,
        GetPayloadEncryptionTypeError

    data class Unexpected(val throwable: Throwable?) :
        CreatePayloadEncryptionKeyPairError,
        GetPayloadEncryptionTypeError
}

sealed interface CreatePayloadEncryptionKeyPairError
sealed interface GetPayloadEncryptionTypeError

fun CreateJWSKeyPairError.toCreatePayloadEncryptionKeyPairError(): CreatePayloadEncryptionKeyPairError = when (this) {
    is KeyPairError.IncompatibleDeviceProofKeyStorage -> PayloadEncryptionError.IncompatibleDeviceProofKeyStorage
    is KeyPairError.UnsupportedProofKeyStorageSecurityLevel -> PayloadEncryptionError.UnsupportedProofKeyStorageSecurityLevel
    is KeyPairError.Unexpected -> PayloadEncryptionError.Unexpected(throwable)
}

fun CreatePayloadEncryptionKeyPairError.toGetPayloadEncryptionTypeError(): GetPayloadEncryptionTypeError = when (this) {
    is PayloadEncryptionError.IncompatibleDeviceProofKeyStorage -> this
    is PayloadEncryptionError.UnsupportedProofKeyStorageSecurityLevel -> this
    is PayloadEncryptionError.Unexpected -> this
}
