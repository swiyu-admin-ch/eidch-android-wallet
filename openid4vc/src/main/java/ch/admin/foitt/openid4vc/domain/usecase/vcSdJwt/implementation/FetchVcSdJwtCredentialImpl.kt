package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation

import ch.admin.foitt.openid4vc.domain.model.BatchCredential
import ch.admin.foitt.openid4vc.domain.model.DeferredCredential
import ch.admin.foitt.openid4vc.domain.model.VerifiableCredential
import ch.admin.foitt.openid4vc.domain.model.VerifiableCredentialParams
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredentialResult
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyVerifiedBatchCredential
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyVerifiedCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchCredentialError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchVerifiableCredentialError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.toFetchCredentialError
import ch.admin.foitt.openid4vc.domain.model.keyBinding.BindingKeyPair
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryptionType
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VerifyVcSdJwtSignatureError
import ch.admin.foitt.openid4vc.domain.usecase.FetchVerifiableCredential
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.FetchVcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.VerifyVcSdJwtSignature
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

internal class FetchVcSdJwtCredentialImpl @Inject constructor(
    private val fetchVerifiableCredential: FetchVerifiableCredential,
    private val verifyVcSdJwtSignature: VerifyVcSdJwtSignature,
) : FetchVcSdJwtCredential {
    override suspend fun invoke(
        verifiableCredentialParams: VerifiableCredentialParams,
        bindingKeyPairs: List<BindingKeyPair>?,
        payloadEncryptionType: PayloadEncryptionType,
    ): Result<AnyCredentialResult, FetchCredentialError> = coroutineBinding {
        val fetchVerifiableCredentialResult = fetchVerifiableCredential(
            verifiableCredentialParams = verifiableCredentialParams,
            bindingKeyPairs = bindingKeyPairs,
            payloadEncryptionType = payloadEncryptionType,
        ).mapError(FetchVerifiableCredentialError::toFetchCredentialError)
            .bind()

        when (fetchVerifiableCredentialResult) {
            is VerifiableCredential -> {
                AnyVerifiedCredential(
                    verifyVcSdJwtSignature(
                        keyBinding = fetchVerifiableCredentialResult.keyBinding,
                        payload = fetchVerifiableCredentialResult.credential
                    ).mapError(VerifyVcSdJwtSignatureError::toFetchCredentialError).bind()
                )
            }

            is BatchCredential -> {
                AnyVerifiedBatchCredential(
                    fetchVerifiableCredentialResult.refreshToken,
                    fetchVerifiableCredentialResult.credentials.map {
                        verifyVcSdJwtSignature(
                            keyBinding = it.keyBinding,
                            payload = it.credential
                        ).mapError(VerifyVcSdJwtSignatureError::toFetchCredentialError).bind()
                    }
                )
            }

            is DeferredCredential -> fetchVerifiableCredentialResult
        }
    }
}
