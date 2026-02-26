package ch.admin.foitt.openid4vc.domain.model.payloadEncryption

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.JWSKeyPair

data class PayloadEncryptionKeyPair(
    val keyPair: JWSKeyPair,
    val alg: String,
    val enc: String,
    val zip: String?,
)
