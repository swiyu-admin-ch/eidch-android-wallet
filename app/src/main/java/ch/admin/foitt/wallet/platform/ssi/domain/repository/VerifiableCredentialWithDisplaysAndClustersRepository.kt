package ch.admin.foitt.wallet.platform.ssi.domain.repository

import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialWithDisplaysAndClusters
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialWithDisplaysRepositoryError
import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow

interface VerifiableCredentialWithDisplaysAndClustersRepository {
    fun getVerifiableCredentialWithDisplaysAndClustersFlowById(
        credentialId: Long
    ): Flow<Result<VerifiableCredentialWithDisplaysAndClusters, CredentialWithDisplaysRepositoryError>>

    fun getNullableVerifiableCredentialWithDisplaysAndClustersFlowById(
        credentialId: Long
    ): Flow<Result<VerifiableCredentialWithDisplaysAndClusters?, CredentialWithDisplaysRepositoryError>>

    fun getVerifiableCredentialsWithDisplaysAndClustersFlow():
        Flow<Result<List<VerifiableCredentialWithDisplaysAndClusters>, CredentialWithDisplaysRepositoryError>>
}
