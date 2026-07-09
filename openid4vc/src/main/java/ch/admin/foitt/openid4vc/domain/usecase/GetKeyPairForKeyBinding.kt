package ch.admin.foitt.openid4vc.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.GetKeyPairForKeyBindingError
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import com.github.michaelbull.result.Result
import java.security.KeyPair

fun interface GetKeyPairForKeyBinding {
    suspend operator fun invoke(keyBinding: KeyBinding): Result<KeyPair, GetKeyPairForKeyBindingError>
}
