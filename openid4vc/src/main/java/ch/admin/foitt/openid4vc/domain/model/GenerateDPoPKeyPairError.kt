package ch.admin.foitt.openid4vc.domain.model

sealed interface GenerateDPoPKeyPairError {
    data object UnsupportedKeyStorageSecurityLevel : GenerateDPoPKeyPairError
    data object IncompatibleDeviceProofKeyStorage : GenerateDPoPKeyPairError
    data object NetworkError : GenerateDPoPKeyPairError
    data class Unexpected(val throwable: Throwable?) : GenerateDPoPKeyPairError
}
