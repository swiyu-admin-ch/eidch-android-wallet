package ch.admin.foitt.openid4vc.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.openid4vc.domain.model.FetchCredentialResult
import ch.admin.foitt.openid4vc.domain.model.VerifiableCredentialParams
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchVerifiableCredentialError
import ch.admin.foitt.openid4vc.domain.model.keyBinding.BindingKeyPair
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryptionType
import com.github.michaelbull.result.Result

internal interface FetchVerifiableCredential {
    @CheckResult
    suspend operator fun invoke(
        isDPopEnabled: Boolean,
        verifiableCredentialParams: VerifiableCredentialParams,
        credentialBindingKeyPairs: List<BindingKeyPair>?,
        dpopKeyPair: BindingKeyPair? = null,
        payloadEncryptionType: PayloadEncryptionType,
    ): Result<FetchCredentialResult, FetchVerifiableCredentialError>
}
