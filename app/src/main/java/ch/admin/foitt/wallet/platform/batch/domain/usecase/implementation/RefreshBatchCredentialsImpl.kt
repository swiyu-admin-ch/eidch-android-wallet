package ch.admin.foitt.wallet.platform.batch.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyVerifiedBatchCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOffer
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchCredentialByConfigError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchIssuerCredentialInfoError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.GetVerifiableCredentialParamsError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.Grant
import ch.admin.foitt.openid4vc.domain.model.threshold
import ch.admin.foitt.openid4vc.domain.usecase.FetchCredentialByConfig
import ch.admin.foitt.openid4vc.domain.usecase.FetchRawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.usecase.GetVerifiableCredentialParams
import ch.admin.foitt.wallet.platform.batch.domain.error.BatchRefreshDataRepositoryError
import ch.admin.foitt.wallet.platform.batch.domain.error.DeleteBundleItemsByAmountError
import ch.admin.foitt.wallet.platform.batch.domain.error.RefreshBatchCredentialsError
import ch.admin.foitt.wallet.platform.batch.domain.model.BatchRefreshParams
import ch.admin.foitt.wallet.platform.batch.domain.repository.BatchRefreshDataRepository
import ch.admin.foitt.wallet.platform.batch.domain.usecase.DeleteBundleItemsByAmount
import ch.admin.foitt.wallet.platform.batch.domain.usecase.RefreshBatchCredentials
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.model.toFetchCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetCredentialConfig
import ch.admin.foitt.wallet.platform.credential.domain.usecase.HandleBatchCredentialResult
import ch.admin.foitt.wallet.platform.holderBinding.domain.model.GenerateProofKeyPairError
import ch.admin.foitt.wallet.platform.holderBinding.domain.usecase.GenerateProofKeyPairs
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.model.GetPayloadEncryptionTypeError
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.usecase.GetPayloadEncryptionType
import ch.admin.foitt.wallet.platform.ssi.domain.model.BundleItemRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.DeleteBundleItemError
import ch.admin.foitt.wallet.platform.ssi.domain.model.toRefreshBatchCredentialsError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.BundleItemRepository
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialRepo
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteBundleItems
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class RefreshBatchCredentialsImpl @Inject constructor(
    private val bundleItemRepository: BundleItemRepository,
    private val batchRefreshDataRepository: BatchRefreshDataRepository,
    private val credentialRepository: CredentialRepo,
    private val getCredentialConfig: GetCredentialConfig,
    private val getPayloadEncryptionType: GetPayloadEncryptionType,
    private val fetchRawAndParsedIssuerCredentialInfo: FetchRawAndParsedIssuerCredentialInfo,
    private val getVerifiableCredentialParams: GetVerifiableCredentialParams,
    private val deleteBundleItemsByAmount: DeleteBundleItemsByAmount,
    private val generateProofKeyPairs: GenerateProofKeyPairs,
    private val fetchCredentialByConfig: FetchCredentialByConfig,
    private val handleBatchCredentialResult: HandleBatchCredentialResult,
    private val deleteBundleItems: DeleteBundleItems,
) : RefreshBatchCredentials {
    override suspend fun invoke(): Result<Unit, RefreshBatchCredentialsError> = coroutineBinding {
        val batchRefreshDataList = batchRefreshDataRepository.getAll()
            .mapError(BatchRefreshDataRepositoryError::toRefreshBatchCredentialsError)
            .bind()

        val countOfNeverPresentedBundleItems = bundleItemRepository.getCountOfNeverPresented()
            .mapError(BundleItemRepositoryError::toRefreshBatchCredentialsError)
            .bind()

        batchRefreshDataList.forEach { batchRefreshData ->
            val presentableBundleItemCount = countOfNeverPresentedBundleItems.find {
                it.credentialId == batchRefreshData.credentialId
            }?.count
            if (presentableBundleItemCount != null && presentableBundleItemCount <= batchRefreshData.batchSize.threshold) {
                val credential = credentialRepository.getById(batchRefreshData.credentialId)
                    .mapError(CredentialRepositoryError::toRefreshBatchCredentialsError)
                    .bind()
                refreshAndSaveCredential(
                    credentialOffer = CredentialOffer(
                        credentialIssuer = credential.issuerUrl,
                        credentialConfigurationIds = listOf(requireNotNull(credential.selectedConfigurationId)),
                        grants = Grant(refreshToken = batchRefreshData.refreshToken)
                    ),
                    batchRefreshParams = BatchRefreshParams(
                        credentialId = batchRefreshData.credentialId,
                        presentableCredentialCount = presentableBundleItemCount,
                        oldBatchSize = batchRefreshData.batchSize
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
        val batchCredentialIssuance = issuerInfo.batchCredentialIssuance
        val batchSize = batchCredentialIssuance?.batchSize
            ?: return@coroutineBinding Err(CredentialError.InvalidIssuerCredentialInfo).bind<FetchCredentialResult>()
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
            batchRefreshDataRepository.saveBatchRefreshData(
                credentialId = batchRefreshParams.credentialId,
                batchSize = batchSize,
                refreshToken = requireNotNull(credentialOffer.grants.refreshToken)
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

        val anyCredentialResult = fetchCredentialByConfig(
            verifiableCredentialParams = verifiableCredentialParams,
            bindingKeyPairs = proofKeyPairs,
            payloadEncryptionType = payloadEncryptionType
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
