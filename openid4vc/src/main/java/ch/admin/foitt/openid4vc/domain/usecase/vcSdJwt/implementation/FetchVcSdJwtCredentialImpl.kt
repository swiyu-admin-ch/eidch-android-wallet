package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation

import ch.admin.foitt.openid4vc.domain.model.BatchCredential
import ch.admin.foitt.openid4vc.domain.model.DeferredCredential
import ch.admin.foitt.openid4vc.domain.model.VerifiableCredential
import ch.admin.foitt.openid4vc.domain.model.VerifiableCredentialParams
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredentialResult
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyVerifiedBatchCredential
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyVerifiedCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchVcSdJwtCredentialError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchVerifiableCredentialError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.toFetchVcSdJwtCredentialError
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
        isDPopEnabled: Boolean,
        verifiableCredentialParams: VerifiableCredentialParams,
        bindingKeyPairs: List<BindingKeyPair>?,
        payloadEncryptionType: PayloadEncryptionType,
        dpopKeyPair: BindingKeyPair?,
    ): Result<AnyCredentialResult, FetchVcSdJwtCredentialError> = coroutineBinding {
        val fetchVerifiableCredentialResult = fetchVerifiableCredential.invoke(
            isDPopEnabled = isDPopEnabled,
            verifiableCredentialParams = verifiableCredentialParams,
            credentialBindingKeyPairs = bindingKeyPairs,
            payloadEncryptionType = payloadEncryptionType,
            dpopKeyPair = dpopKeyPair,
        ).mapError(FetchVerifiableCredentialError::toFetchVcSdJwtCredentialError)
            .bind()

        when (fetchVerifiableCredentialResult) {
            is VerifiableCredential -> {
                AnyVerifiedCredential(
                    verifyVcSdJwtSignature(
                        keyBinding = fetchVerifiableCredentialResult.keyBinding,
                        payload = fetchVerifiableCredentialResult.credential,
                        format = verifiableCredentialParams.credentialConfiguration.format,
                    ).mapError(VerifyVcSdJwtSignatureError::toFetchVcSdJwtCredentialError).bind()
                )
            }

            is BatchCredential -> {
                AnyVerifiedBatchCredential(
                    fetchVerifiableCredentialResult.accessToken,
                    fetchVerifiableCredentialResult.refreshToken,
                    fetchVerifiableCredentialResult.dpopKeyBinding,
                    fetchVerifiableCredentialResult.credentials.map {
                        verifyVcSdJwtSignature(
                            keyBinding = it.keyBinding,
                            payload = it.credential,
                            format = verifiableCredentialParams.credentialConfiguration.format,
                        ).mapError(VerifyVcSdJwtSignatureError::toFetchVcSdJwtCredentialError).bind()
                    }
                )
            }

            is DeferredCredential -> fetchVerifiableCredentialResult
        }
    }
}
