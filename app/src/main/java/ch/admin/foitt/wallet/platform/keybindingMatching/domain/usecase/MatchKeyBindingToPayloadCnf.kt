package ch.admin.foitt.wallet.platform.keybindingMatching.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import ch.admin.foitt.wallet.platform.keybindingMatching.domain.model.MatchKeyBindingToPayloadCnfError
import com.github.michaelbull.result.Result

interface MatchKeyBindingToPayloadCnf {
    suspend operator fun invoke(
        keyBindings: List<KeyBinding?>,
        payload: String,
    ): Result<KeyBinding, MatchKeyBindingToPayloadCnfError>
}
