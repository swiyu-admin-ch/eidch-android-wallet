package ch.admin.foitt.openid4vc.domain.model

sealed interface GetKeyPairForKeyBindingError {
    data object SoftwareKeyNotFound : GetKeyPairForKeyBindingError
    data object HardwareKeyNotFound : GetKeyPairForKeyBindingError
    data class Unexpected(val throwable: Throwable) : GetKeyPairForKeyBindingError
}

internal fun GetKeyPairError.toGetKeyPairForKeyBindingError(): GetKeyPairForKeyBindingError = when (this) {
    KeyPairError.NotFound -> GetKeyPairForKeyBindingError.HardwareKeyNotFound
    is KeyPairError.Unexpected -> GetKeyPairForKeyBindingError.Unexpected(throwable)
}

internal fun GetSoftwareKeyPairError.toGetKeyPairForKeyBindingError(): GetKeyPairForKeyBindingError = when (this) {
    is KeyPairError.Unexpected -> GetKeyPairForKeyBindingError.Unexpected(throwable)
}
