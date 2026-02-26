package ch.admin.foitt.wallet.platform.payloadEncryption.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialRequestEncryption
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialResponseEncryption
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryptionType
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.model.CreatePayloadEncryptionKeyPairError
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.model.GetPayloadEncryptionTypeError
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.model.toGetPayloadEncryptionTypeError
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.usecase.CreatePayloadEncryptionKeyPair
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.usecase.GetPayloadEncryptionType
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class GetPayloadEncryptionTypeImpl @Inject constructor(
    private val createPayloadEncryptionKeyPair: CreatePayloadEncryptionKeyPair,
) : GetPayloadEncryptionType {
    override suspend fun invoke(
        requestEncryption: CredentialRequestEncryption?,
        responseEncryption: CredentialResponseEncryption?
    ): Result<PayloadEncryptionType, GetPayloadEncryptionTypeError> = coroutineBinding {
        if (requestEncryption != null && responseEncryption != null) {
            val keyPair = createPayloadEncryptionKeyPair(responseEncryption)
                .mapError(CreatePayloadEncryptionKeyPairError::toGetPayloadEncryptionTypeError)
                .bind()

            PayloadEncryptionType.Response(
                requestEncryption = requestEncryption,
                responseEncryption = responseEncryption,
                responseEncryptionKeyPair = keyPair
            )
        } else if (requestEncryption != null) {
            PayloadEncryptionType.Request(
                requestEncryption = requestEncryption
            )
        } else {
            PayloadEncryptionType.None
        }
    }
}
