package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.BatchSize
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyVerifiedBatchCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.wallet.platform.batch.domain.error.BatchRefreshDataRepositoryError
import ch.admin.foitt.wallet.platform.batch.domain.repository.BatchRefreshDataRepository
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.HandleBatchCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.SaveVcSdJwtCredentials
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import java.net.URL
import javax.inject.Inject

class HandleBatchCredentialResultImpl @Inject constructor(
    private val saveVcSdJwtCredentials: SaveVcSdJwtCredentials,
    private val batchRefreshDataRepository: BatchRefreshDataRepository,
) : HandleBatchCredentialResult {
    override suspend fun invoke(
        credentialId: Long,
        issuerUrl: URL,
        batchSize: BatchSize,
        anyVerifiedBatchCredential: AnyVerifiedBatchCredential,
        rawAndParsedCredentialInfo: RawAndParsedIssuerCredentialInfo,
        credentialConfig: AnyCredentialConfiguration,
    ): Result<FetchCredentialResult, FetchCredentialError> = coroutineBinding {
        val savedCredentialId = saveVcSdJwtCredentials(
            credentialId = credentialId,
            issuerUrl = issuerUrl,
            vcSdJwtCredentials = anyVerifiedBatchCredential.vcSdJwtCredentials,
            rawAndParsedCredentialInfo = rawAndParsedCredentialInfo,
            credentialConfig = credentialConfig,
        ).bind()

        anyVerifiedBatchCredential.refreshToken?.let { refreshToken ->
            batchRefreshDataRepository.saveBatchRefreshData(
                credentialId = savedCredentialId,
                batchSize = batchSize,
                refreshToken = refreshToken
            ).mapError(BatchRefreshDataRepositoryError::toFetchCredentialError).bind()
        }
        FetchCredentialResult.Credential(savedCredentialId)
    }
}
