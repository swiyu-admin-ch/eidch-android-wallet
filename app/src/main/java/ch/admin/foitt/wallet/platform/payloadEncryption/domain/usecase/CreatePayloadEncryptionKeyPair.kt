package ch.admin.foitt.wallet.platform.payloadEncryption.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialResponseEncryption
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryptionKeyPair
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.model.CreatePayloadEncryptionKeyPairError
import com.github.michaelbull.result.Result

interface CreatePayloadEncryptionKeyPair {
    suspend operator fun invoke(
        credentialResponseEncryption: CredentialResponseEncryption
    ): Result<PayloadEncryptionKeyPair, CreatePayloadEncryptionKeyPairError>
}
