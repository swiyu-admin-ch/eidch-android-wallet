package ch.admin.foitt.wallet.platform.payloadEncryption.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialResponseEncryption
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryptionKeyPair
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.model.CreateJWSKeyPairError
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.usecase.CreateJWSKeyPairInSoftware
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.model.CreatePayloadEncryptionKeyPairError
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.model.toCreatePayloadEncryptionKeyPairError
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.usecase.CreatePayloadEncryptionKeyPair
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class CreatePayloadEncryptionKeyPairImpl @Inject constructor(
    private val createJWSKeyPairInSoftware: CreateJWSKeyPairInSoftware,
) : CreatePayloadEncryptionKeyPair {
    override suspend fun invoke(
        credentialResponseEncryption: CredentialResponseEncryption,
    ): Result<PayloadEncryptionKeyPair, CreatePayloadEncryptionKeyPairError> = coroutineBinding {
        val keyPair = createJWSKeyPairInSoftware(SigningAlgorithm.ES256)
            .mapError(CreateJWSKeyPairError::toCreatePayloadEncryptionKeyPairError)
            .bind()

        PayloadEncryptionKeyPair(
            keyPair = keyPair,
            alg = credentialResponseEncryption.algValuesSupported.first(),
            enc = credentialResponseEncryption.encValuesSupported.first(),
            zip = credentialResponseEncryption.zipValuesSupported?.firstOrNull(),
        )
    }
}
