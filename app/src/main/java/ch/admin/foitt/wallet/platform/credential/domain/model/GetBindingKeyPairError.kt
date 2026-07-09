package ch.admin.foitt.wallet.platform.credential.domain.model

import ch.admin.foitt.openid4vc.domain.model.GetKeyPairForKeyBindingError

sealed interface GetBindingKeyPairError {
    data object HardwareKeyNotFound : GetBindingKeyPairError
    data object MissingSoftwareKeyMaterial : GetBindingKeyPairError
    data class Unexpected(val throwable: Throwable) : GetBindingKeyPairError
}

fun GetKeyPairForKeyBindingError.toGetBindingKeyPairError(): GetBindingKeyPairError = when (this) {
    GetKeyPairForKeyBindingError.SoftwareKeyNotFound -> GetBindingKeyPairError.MissingSoftwareKeyMaterial
    GetKeyPairForKeyBindingError.HardwareKeyNotFound -> GetBindingKeyPairError.HardwareKeyNotFound
    is GetKeyPairForKeyBindingError.Unexpected -> GetBindingKeyPairError.Unexpected(throwable)
}
