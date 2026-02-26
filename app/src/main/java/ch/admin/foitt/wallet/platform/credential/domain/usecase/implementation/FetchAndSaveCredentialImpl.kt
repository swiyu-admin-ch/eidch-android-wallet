package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyDeferredCredential
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyVerifiedBatchCredential
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyVerifiedCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOffer
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchCredentialByConfigError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchIssuerCredentialInfoError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.PrepareFetchVerifiableCredentialError
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryptionType
import ch.admin.foitt.openid4vc.domain.usecase.FetchCredentialByConfig
import ch.admin.foitt.openid4vc.domain.usecase.FetchRawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.usecase.GetVerifiableCredentialParams
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.model.toFetchCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchAndSaveCredential
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetCredentialConfig
import ch.admin.foitt.wallet.platform.credential.domain.usecase.HandleBatchCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.HandleCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.HandleDeferredCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.ValidateIssuerCredentialInfo
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.holderBinding.domain.model.GenerateProofKeyPairError
import ch.admin.foitt.wallet.platform.holderBinding.domain.usecase.GenerateProofKeyPairs
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.model.GetPayloadEncryptionTypeError
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.usecase.GetPayloadEncryptionType
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class FetchAndSaveCredentialImpl @Inject constructor(
    private val fetchRawAndParsedIssuerCredentialInfo: FetchRawAndParsedIssuerCredentialInfo,
    private val validateIssuerCredentialInfo: ValidateIssuerCredentialInfo,
    private val getPayloadEncryptionType: GetPayloadEncryptionType,
    private val getVerifiableCredentialParams: GetVerifiableCredentialParams,
    private val getCredentialConfig: GetCredentialConfig,
    private val generateProofKeyPairs: GenerateProofKeyPairs,
    private val fetchCredentialByConfig: FetchCredentialByConfig,
    private val handleCredentialResult: HandleCredentialResult,
    private val handleBatchCredentialResult: HandleBatchCredentialResult,
    private val handleDeferredCredentialResult: HandleDeferredCredentialResult,
    private val environmentSetupRepository: EnvironmentSetupRepository,
) : FetchAndSaveCredential {
    override suspend fun invoke(
        credentialOffer: CredentialOffer,
    ): Result<FetchCredentialResult, FetchCredentialError> = coroutineBinding {
        val rawAndParsedCredentialInfo =
            fetchRawAndParsedIssuerCredentialInfo(credentialOffer.credentialIssuer)
                .mapError(FetchIssuerCredentialInfoError::toFetchCredentialError)
                .bind()

        val issuerInfo = rawAndParsedCredentialInfo.issuerCredentialInfo

        val payloadEncryptionType = if (environmentSetupRepository.payloadEncryptionEnabled) {
            val isConfigValid = validateIssuerCredentialInfo(issuerInfo)
            if (!isConfigValid) {
                return@coroutineBinding Err(CredentialError.InvalidIssuerCredentialInfo).bind<FetchCredentialResult>()
            }

            getPayloadEncryptionType(
                requestEncryption = issuerInfo.credentialRequestEncryption,
                responseEncryption = issuerInfo.credentialResponseEncryption,
            ).mapError(GetPayloadEncryptionTypeError::toFetchCredentialError)
                .bind()
        } else {
            PayloadEncryptionType.None
        }

        val config = getCredentialConfig(
            credentials = credentialOffer.credentialConfigurationIds,
            credentialConfigurations = issuerInfo.credentialConfigurations
        ).bind()

        val verifiableCredentialParams = getVerifiableCredentialParams(
            issuerCredentialInfo = issuerInfo,
            credentialConfiguration = config,
            credentialOffer = credentialOffer,
        ).mapError(PrepareFetchVerifiableCredentialError::toFetchCredentialError).bind()

        val batchSize = if (environmentSetupRepository.batchIssuanceEnabled.not() && verifiableCredentialParams.isBatch) {
            // This is a workaround for the BETA-ID, which is already issued as a batch
            1
        } else {
            issuerInfo.batchCredentialIssuance?.batchSize ?: 1
        }
        val proofKeyPairs = verifiableCredentialParams.proofTypeConfig?.let { proofTypeConfig ->
            generateProofKeyPairs(batchSize, proofTypeConfig)
                .mapError(GenerateProofKeyPairError::toFetchCredentialError)
                .bind()
        }

        val anyCredentialResult = fetchCredentialByConfig(
            verifiableCredentialParams = verifiableCredentialParams,
            bindingKeyPairs = proofKeyPairs,
            payloadEncryptionType = payloadEncryptionType,
        ).mapError(FetchCredentialByConfigError::toFetchCredentialError).bind()

        when (anyCredentialResult) {
            is AnyVerifiedCredential -> handleCredentialResult(
                issuerUrl = credentialOffer.credentialIssuer,
                anyVerifiedCredential = anyCredentialResult,
                rawAndParsedCredentialInfo = rawAndParsedCredentialInfo,
                credentialConfig = config,
            ).bind()

            is AnyVerifiedBatchCredential -> handleBatchCredentialResult(
                issuerUrl = credentialOffer.credentialIssuer,
                batchSize = batchSize,
                anyVerifiedBatchCredential = anyCredentialResult,
                rawAndParsedCredentialInfo = rawAndParsedCredentialInfo,
                credentialConfig = config,
            ).bind()

            is AnyDeferredCredential -> handleDeferredCredentialResult(
                issuerUrl = credentialOffer.credentialIssuer,
                deferredCredential = anyCredentialResult,
                rawAndParsedCredentialInfo = rawAndParsedCredentialInfo,
                credentialConfig = config,
            ).bind()
        }
    }
}
