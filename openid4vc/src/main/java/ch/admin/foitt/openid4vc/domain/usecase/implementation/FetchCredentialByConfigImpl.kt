package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.VerifiableCredentialParams
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredentialResult
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchCredentialByConfigError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchVcSdJwtCredentialError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.UnknownCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.VcSdJwtCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.toFetchCredentialByConfigError
import ch.admin.foitt.openid4vc.domain.model.keyBinding.BindingKeyPair
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryptionType
import ch.admin.foitt.openid4vc.domain.usecase.FetchCredentialByConfig
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.FetchVcSdJwtCredential
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.mapError
import javax.inject.Inject

internal class FetchCredentialByConfigImpl @Inject constructor(
    private val fetchVcSdJwtCredential: FetchVcSdJwtCredential,
) : FetchCredentialByConfig {
    override suspend fun invoke(
        verifiableCredentialParams: VerifiableCredentialParams,
        bindingKeyPairs: List<BindingKeyPair>?,
        payloadEncryptionType: PayloadEncryptionType,
    ): Result<AnyCredentialResult, FetchCredentialByConfigError> =
        when (verifiableCredentialParams.credentialConfiguration) {
            is VcSdJwtCredentialConfiguration -> {
                fetchVcSdJwtCredential(
                    verifiableCredentialParams = verifiableCredentialParams,
                    bindingKeyPairs = bindingKeyPairs,
                    payloadEncryptionType = payloadEncryptionType,
                ).mapError(FetchVcSdJwtCredentialError::toFetchCredentialByConfigError)
            }
            is UnknownCredentialConfiguration -> Err(CredentialOfferError.UnsupportedCredentialFormat)
        }
}
