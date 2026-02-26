package ch.admin.foitt.wallet.platform.payloadEncryption.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialRequestEncryption
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialResponseEncryption
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryptionType
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.model.GetPayloadEncryptionTypeError
import com.github.michaelbull.result.Result

interface GetPayloadEncryptionType {
    suspend operator fun invoke(
        requestEncryption: CredentialRequestEncryption?,
        responseEncryption: CredentialResponseEncryption?
    ): Result<PayloadEncryptionType, GetPayloadEncryptionTypeError>
}
