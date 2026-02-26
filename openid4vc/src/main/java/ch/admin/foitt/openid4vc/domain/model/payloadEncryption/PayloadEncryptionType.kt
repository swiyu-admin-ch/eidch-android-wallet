package ch.admin.foitt.openid4vc.domain.model.payloadEncryption

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialRequestEncryption
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialResponseEncryption

sealed interface PayloadEncryptionType {
    data object None : PayloadEncryptionType

    data class Request(
        val requestEncryption: CredentialRequestEncryption,
    ) : PayloadEncryptionType

    // Response encryption can only be done when request encryption is also done
    data class Response(
        val requestEncryption: CredentialRequestEncryption,
        val responseEncryption: CredentialResponseEncryption,
        val responseEncryptionKeyPair: PayloadEncryptionKeyPair,
    ) : PayloadEncryptionType
}
