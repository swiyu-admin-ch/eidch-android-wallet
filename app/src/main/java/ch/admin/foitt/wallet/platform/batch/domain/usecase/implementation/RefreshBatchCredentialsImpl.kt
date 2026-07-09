package ch.admin.foitt.wallet.platform.batch.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.GenerateDPoPKeyPairError
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyVerifiedBatchCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOffer
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchCredentialByConfigError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchIssuerCredentialInfoError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.GetVerifiableCredentialParamsError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.Grant
import ch.admin.foitt.openid4vc.domain.model.threshold
import ch.admin.foitt.openid4vc.domain.usecase.FetchCredentialByConfig
import ch.admin.foitt.openid4vc.domain.usecase.FetchRawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.usecase.GenerateDPoPKeyPair
import ch.admin.foitt.openid4vc.domain.usecase.GetVerifiableCredentialParams
import ch.admin.foitt.wallet.platform.batch.domain.error.BatchRefreshDataRepositoryError
import ch.admin.foitt.wallet.platform.batch.domain.error.DeleteBundleItemsByAmountError
import ch.admin.foitt.wallet.platform.batch.domain.error.RefreshBatchCredentialsError
import ch.admin.foitt.wallet.platform.batch.domain.model.BatchRefreshParams
import ch.admin.foitt.wallet.platform.batch.domain.repository.BatchRefreshDataRepository
import ch.admin.foitt.wallet.platform.batch.domain.usecase.DeleteBundleItemsByAmount
import ch.admin.foitt.wallet.platform.batch.domain.usecase.RefreshBatchCredentials
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.model.GetBindingKeyPairError
import ch.admin.foitt.wallet.platform.credential.domain.model.toFetchCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.EvaluateBatchSize
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetBindingKeyPair
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetCredentialConfig
import ch.admin.foitt.wallet.platform.credential.domain.usecase.HandleBatchCredentialResult
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.holderBinding.domain.model.GenerateProofKeyPairError
import ch.admin.foitt.wallet.platform.holderBinding.domain.usecase.GenerateProofKeyPairs
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.model.GetPayloadEncryptionTypeError
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.usecase.GetPayloadEncryptionType
import ch.admin.foitt.wallet.platform.ssi.domain.model.BundleItemRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialWithBatchDataAndAuthenticationRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.DeleteBundleItemError
import ch.admin.foitt.wallet.platform.ssi.domain.model.toRefreshBatchCredentialsError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.BundleItemRepository
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialWithBatchDataAndAuthenticationRepository
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteBundleItems
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class RefreshBatchCredentialsImpl @Inject constructor(
    private val bundleItemRepository: BundleItemRepository,
    private val batchRefreshDataRepository: BatchRefreshDataRepository,
    private val verifiableCredentialWithBatchDataAndAuthenticationRepository: VerifiableCredentialWithBatchDataAndAuthenticationRepository,
    private val getCredentialConfig: GetCredentialConfig,
    private val getPayloadEncryptionType: GetPayloadEncryptionType,
    private val fetchRawAndParsedIssuerCredentialInfo: FetchRawAndParsedIssuerCredentialInfo,
    private val getVerifiableCredentialParams: GetVerifiableCredentialParams,
    private val evaluateBatchSize: EvaluateBatchSize,
    private val deleteBundleItemsByAmount: DeleteBundleItemsByAmount,
    private val generateProofKeyPairs: GenerateProofKeyPairs,
    private val environmentSetupRepository: EnvironmentSetupRepository,
    private val fetchCredentialByConfig: FetchCredentialByConfig,
    private val getBindingKeyPair: GetBindingKeyPair,
    private val handleBatchCredentialResult: HandleBatchCredentialResult,
    private val deleteBundleItems: DeleteBundleItems,
    private val generateDPoPKeyPair: GenerateDPoPKeyPair,
) : RefreshBatchCredentials {
    override suspend fun invoke(): Result<Unit, RefreshBatchCredentialsError> = coroutineBinding {
        val batchRefreshDataList = verifiableCredentialWithBatchDataAndAuthenticationRepository.getAll()
            .mapError(CredentialWithBatchDataAndAuthenticationRepositoryError::toRefreshBatchCredentialsError)
            .bind()

        val countOfNeverPresentedBundleItems = bundleItemRepository.getCountOfNeverPresented()
            .mapError(BundleItemRepositoryError::toRefreshBatchCredentialsError)
            .bind()

        batchRefreshDataList.forEach { verifiableCredentialWithBatchDataAndAuthentication ->
            val batchData = verifiableCredentialWithBatchDataAndAuthentication.batchData
            val presentableBundleItemCount = countOfNeverPresentedBundleItems.find {
                it.credentialId == batchData.credentialId
            }?.count
            val refreshToken = verifiableCredentialWithBatchDataAndAuthentication.authentication.refreshToken
            if (refreshToken != null &&
                presentableBundleItemCount != null &&
                presentableBundleItemCount <= batchData.batchSize.threshold
            ) {
                val credential = verifiableCredentialWithBatchDataAndAuthentication.credential
                refreshAndSaveCredential(
                    credentialOffer = CredentialOffer(
                        credentialIssuer = credential.issuerUrl,
                        credentialConfigurationIds = listOf(requireNotNull(credential.selectedConfigurationId)),
                        grants = Grant(
                            refreshToken = refreshToken
                        )
                    ),
                    batchRefreshParams = BatchRefreshParams(
                        credentialId = batchData.credentialId,
                        presentableCredentialCount = presentableBundleItemCount,
                        oldBatchSize = batchData.batchSize,
                        authentication = verifiableCredentialWithBatchDataAndAuthentication.authentication,
                    )
                )
            }
        }
    }

    private suspend fun refreshAndSaveCredential(
        credentialOffer: CredentialOffer,
        batchRefreshParams: BatchRefreshParams
    ): Result<FetchCredentialResult, FetchCredentialError> = coroutineBinding {
        val rawAndParsedCredentialInfo = fetchRawAndParsedIssuerCredentialInfo(
            issuerEndpoint = credentialOffer.credentialIssuer,
        ).mapError(FetchIssuerCredentialInfoError::toFetchCredentialError)
            .bind()

        val issuerInfo = rawAndParsedCredentialInfo.issuerCredentialInfo
        val batchSize = evaluateBatchSize(issuerInfo).bind()
        val config = getCredentialConfig(
            credentials = credentialOffer.credentialConfigurationIds,
            credentialConfigurations = issuerInfo.credentialConfigurations
        ).bind()

        val needToReducePresentableCredentialCount = batchSize < batchRefreshParams.presentableCredentialCount
        val onlyUpdateRefreshData =
            batchSize.threshold < batchRefreshParams.presentableCredentialCount || needToReducePresentableCredentialCount

        if (needToReducePresentableCredentialCount) {
            deleteBundleItemsByAmount(batchRefreshParams.credentialId, batchRefreshParams.oldBatchSize - batchSize)
                .mapError(DeleteBundleItemsByAmountError::toFetchCredentialError)
        }

        if (onlyUpdateRefreshData) {
            batchRefreshDataRepository.updateBatchSize(
                credentialId = batchRefreshParams.credentialId,
                batchSize = batchSize,
            ).mapError(BatchRefreshDataRepositoryError::toFetchCredentialError).bind()
            return@coroutineBinding FetchCredentialResult.Credential(batchRefreshParams.credentialId)
        }

        val verifiableCredentialParams = getVerifiableCredentialParams(
            credentialConfiguration = config,
            credentialOffer = credentialOffer,
            issuerCredentialInfo = issuerInfo
        ).mapError(GetVerifiableCredentialParamsError::toFetchCredentialError).bind()

        val proofKeyPairs = verifiableCredentialParams.proofTypeConfig?.let { proofTypeConfig ->
            generateProofKeyPairs(
                amount = batchSize,
                proofTypeConfig = proofTypeConfig
            ).mapError(GenerateProofKeyPairError::toFetchCredentialError)
                .bind()
        }

        val payloadEncryptionType = getPayloadEncryptionType(
            requestEncryption = issuerInfo.credentialRequestEncryption,
            responseEncryption = issuerInfo.credentialResponseEncryption,
        ).mapError(GetPayloadEncryptionTypeError::toFetchCredentialError).bind()

        val dpopKeyPair = getBindingKeyPair(batchRefreshParams.authentication)
            .mapError(GetBindingKeyPairError::toFetchCredentialError)
            .bind() ?: generateDPoPKeyPair(verifiableCredentialParams)
            .mapError(GenerateDPoPKeyPairError::toFetchCredentialError)
            .bind()

        val anyCredentialResult = fetchCredentialByConfig(
            isDPopEnabled = environmentSetupRepository.isDPopEnabled,
            verifiableCredentialParams = verifiableCredentialParams,
            bindingKeyPairs = proofKeyPairs,
            payloadEncryptionType = payloadEncryptionType,
            dpopKeyPair = dpopKeyPair,
        ).mapError(FetchCredentialByConfigError::toFetchCredentialError).bind()

        val oldBundleItems = bundleItemRepository.getAllByCredentialId(batchRefreshParams.credentialId)
            .mapError(BundleItemRepositoryError::toFetchCredentialError)
            .bind()

        val result = when (anyCredentialResult) {
            is AnyVerifiedBatchCredential -> handleBatchCredentialResult(
                credentialId = batchRefreshParams.credentialId,
                issuerUrl = credentialOffer.credentialIssuer,
                batchSize = batchSize,
                anyVerifiedBatchCredential = anyCredentialResult,
                rawAndParsedCredentialInfo = rawAndParsedCredentialInfo,
                credentialConfig = config,
            ).bind()

            else -> error(IllegalStateException("Only batch credentials can be refreshed"))
        }

        deleteBundleItems(oldBundleItems.map { it.id })
            .mapError(DeleteBundleItemError::toFetchCredentialError)
            .bind()
        return@coroutineBinding result
    }
}
