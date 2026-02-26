package ch.admin.foitt.wallet.platform.holderBinding.domain.model

import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AttestationError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.RequestKeyAttestationError
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.model.CreateJWSKeyPairError
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.model.KeyPairError

interface HolderBindingError {
    data object UnsupportedCryptographicSuite : GenerateProofKeyPairError
    data object UnsupportedProofKeyStorageSecurityLevel : GenerateProofKeyPairError
    data object IncompatibleDeviceProofKeyStorage : GenerateProofKeyPairError
    data object InvalidProofKeyAttestation : GenerateProofKeyPairError
    data class Unexpected(val throwable: Throwable?) : GenerateProofKeyPairError
}

sealed interface GenerateProofKeyPairError

fun CreateJWSKeyPairError.toGenerateProofKeyPairError(): GenerateProofKeyPairError = when (this) {
    is KeyPairError.UnsupportedProofKeyStorageSecurityLevel -> HolderBindingError.UnsupportedProofKeyStorageSecurityLevel
    is KeyPairError.IncompatibleDeviceProofKeyStorage -> HolderBindingError.IncompatibleDeviceProofKeyStorage
    is KeyPairError.Unexpected -> HolderBindingError.Unexpected(throwable)
}

fun RequestKeyAttestationError.toGenerateProofKeyPairError(): GenerateProofKeyPairError = when (this) {
    is AttestationError.UnsupportedKeyStorageSecurityLevel -> HolderBindingError.UnsupportedProofKeyStorageSecurityLevel
    is AttestationError.IncompatibleDeviceKeyStorage -> HolderBindingError.IncompatibleDeviceProofKeyStorage
    is AttestationError.ValidationError -> HolderBindingError.InvalidProofKeyAttestation
    is AttestationError.NetworkError -> HolderBindingError.Unexpected(null)
    is AttestationError.Unexpected -> HolderBindingError.Unexpected(throwable)
}
