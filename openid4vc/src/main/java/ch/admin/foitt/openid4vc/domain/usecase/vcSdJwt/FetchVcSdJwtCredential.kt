package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt

import androidx.annotation.CheckResult
import ch.admin.foitt.openid4vc.domain.model.VerifiableCredentialParams
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredentialResult
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchVcSdJwtCredentialError
import ch.admin.foitt.openid4vc.domain.model.keyBinding.BindingKeyPair
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryptionType
import com.github.michaelbull.result.Result

internal interface FetchVcSdJwtCredential {
    @CheckResult
    suspend operator fun invoke(
        isDPopEnabled: Boolean,
        verifiableCredentialParams: VerifiableCredentialParams,
        bindingKeyPairs: List<BindingKeyPair>?,
        payloadEncryptionType: PayloadEncryptionType,
        dpopKeyPair: BindingKeyPair? = null,
    ): Result<AnyCredentialResult, FetchVcSdJwtCredentialError>
}
