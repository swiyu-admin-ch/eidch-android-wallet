package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.GetKeyPairError
import ch.admin.foitt.openid4vc.domain.model.GetKeyPairForKeyBindingError
import ch.admin.foitt.openid4vc.domain.model.GetSoftwareKeyPairError
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBindingType
import ch.admin.foitt.openid4vc.domain.model.toGetKeyPairForKeyBindingError
import ch.admin.foitt.openid4vc.domain.usecase.GetHardwareKeyPair
import ch.admin.foitt.openid4vc.domain.usecase.GetKeyPairForKeyBinding
import ch.admin.foitt.openid4vc.domain.usecase.GetSoftwareKeyPair
import ch.admin.foitt.openid4vc.utils.Constants.ANDROID_KEY_STORE
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import java.security.KeyPair
import javax.inject.Inject

internal class GetKeyPairForKeyBindingImpl @Inject constructor(
    private val getHardwareKeyPair: GetHardwareKeyPair,
    private val getSoftwareKeyPair: GetSoftwareKeyPair,
) : GetKeyPairForKeyBinding {
    override suspend fun invoke(
        keyBinding: KeyBinding,
    ): Result<KeyPair, GetKeyPairForKeyBindingError> = coroutineBinding {
        when (keyBinding.bindingType) {
            KeyBindingType.SOFTWARE -> {
                val publicKey = keyBinding.publicKey
                val privateKey = keyBinding.privateKey
                if (publicKey != null && privateKey != null) {
                    getSoftwareKeyPair(publicKey, privateKey)
                        .mapError(GetSoftwareKeyPairError::toGetKeyPairForKeyBindingError)
                        .bind()
                } else {
                    Err(GetKeyPairForKeyBindingError.SoftwareKeyNotFound).bind()
                }
            }
            KeyBindingType.HARDWARE -> getHardwareKeyPair(keyBinding.identifier, ANDROID_KEY_STORE)
                .mapError(GetKeyPairError::toGetKeyPairForKeyBindingError)
                .bind()
        }
    }
}
