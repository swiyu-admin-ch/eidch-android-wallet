package ch.admin.foitt.openid4vc.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.CredentialRequestType
import ch.admin.foitt.openid4vc.domain.model.CredentialType
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CreateCredentialRequestError
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryptionType
import com.github.michaelbull.result.Result

interface CreateCredentialRequest {
    suspend operator fun invoke(
        payloadEncryptionType: PayloadEncryptionType,
        credentialType: CredentialType,
    ): Result<CredentialRequestType, CreateCredentialRequestError>
}
