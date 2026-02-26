package ch.admin.foitt.wallet.platform.keyPairGenerator.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.JWSKeyPair
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBindingType
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.model.KeyPairError
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.usecase.CreateJWSKeyPairInSoftware
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.spec.ECGenParameterSpec
import java.util.UUID
import javax.inject.Inject

internal class CreateJWSKeyPairInSoftwareImpl @Inject constructor() : CreateJWSKeyPairInSoftware {

    override suspend operator fun invoke(signingAlgorithm: SigningAlgorithm) = runSuspendCatching {
        val keyId = UUID.randomUUID().toString()
        val keyPair = createKeyPairInSoftware()

        JWSKeyPair(
            keyId = keyId,
            algorithm = signingAlgorithm,
            keyPair = keyPair,
            bindingType = KeyBindingType.SOFTWARE,
        )
    }.mapError { throwable ->
        KeyPairError.Unexpected(throwable)
    }

    private fun createKeyPairInSoftware(): KeyPair {
        val generator = KeyPairGenerator.getInstance("EC")
        val spec = ECGenParameterSpec("secp256r1")
        generator.initialize(spec)
        return generator.generateKeyPair()
    }
}
