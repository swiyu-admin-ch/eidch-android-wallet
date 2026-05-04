package ch.admin.foitt.openid4vc.domain.model.anycredential

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import java.net.URL

interface AnyDeferredCredential : AnyCredentialResult {
    val format: CredentialFormat
    val transactionId: String
    val accessToken: String
    val refreshToken: String?
    val endpoint: URL
    val pollInterval: Int
    val keyBindings: List<KeyBinding>?
}
