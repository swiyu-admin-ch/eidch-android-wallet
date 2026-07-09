package ch.admin.foitt.wallet.platform.holderBinding.domain.usecase.implementation.mocks

import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.JWSKeyPair
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBindingType
import io.mockk.mockk
import java.security.KeyPair

object KeyPairMocks {
    private const val KEY_ID = "keyId"
    private val mockKeyPair = mockk<KeyPair>()

    val validKeyPairES256Software = JWSKeyPair(
        algorithm = SigningAlgorithm.ES256,
        keyPair = mockKeyPair,
        keyId = KEY_ID,
        bindingType = KeyBindingType.SOFTWARE,
    )

    val validKeyPairES256Hardware = JWSKeyPair(
        algorithm = SigningAlgorithm.ES256,
        keyPair = mockKeyPair,
        keyId = KEY_ID,
        bindingType = KeyBindingType.HARDWARE
    )

    val validKeyPairES512 = JWSKeyPair(
        algorithm = SigningAlgorithm.ES512,
        keyPair = mockKeyPair,
        keyId = KEY_ID,
        bindingType = KeyBindingType.SOFTWARE,
    )
}
