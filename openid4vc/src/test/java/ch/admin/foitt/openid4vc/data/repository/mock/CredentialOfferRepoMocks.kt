package ch.admin.foitt.openid4vc.data.repository.mock

import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.spec.ECGenParameterSpec

object CredentialOfferRepoMocks {
    val mockSoftwareKeyPair = generateKeyPair()
}

private fun generateKeyPair(): KeyPair {
    val generator = KeyPairGenerator.getInstance("EC")
    val spec = ECGenParameterSpec("secp256r1")
    generator.initialize(spec)
    return generator.generateKeyPair()
}
