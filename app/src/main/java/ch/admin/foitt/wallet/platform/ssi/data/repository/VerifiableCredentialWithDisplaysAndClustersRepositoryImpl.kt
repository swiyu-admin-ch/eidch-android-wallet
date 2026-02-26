package ch.admin.foitt.wallet.platform.ssi.data.repository

import ch.admin.foitt.wallet.platform.database.data.dao.DaoProvider
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialWithDisplaysAndClusters
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialWithDisplaysRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.toVerifiableCredentialWithDisplaysAndClustersRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialWithDisplaysAndClustersRepository
import ch.admin.foitt.wallet.platform.utils.catchAndMap
import com.github.michaelbull.result.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class VerifiableCredentialWithDisplaysAndClustersRepositoryImpl @Inject constructor(
    daoProvider: DaoProvider,
) : VerifiableCredentialWithDisplaysAndClustersRepository {

    override fun getVerifiableCredentialWithDisplaysAndClustersFlowById(
        credentialId: Long
    ): Flow<Result<VerifiableCredentialWithDisplaysAndClusters, CredentialWithDisplaysRepositoryError>> =
        daoFlow.flatMapLatest { dao ->
            dao?.getVerifiableCredentialWithDisplaysAndClustersFlowById(credentialId)
                ?.catchAndMap { throwable ->
                    throwable.toVerifiableCredentialWithDisplaysAndClustersRepositoryError("Error to get VerifiableCredentialWithDetail")
                } ?: emptyFlow()
        }

    override fun getNullableVerifiableCredentialWithDisplaysAndClustersFlowById(
        credentialId: Long
    ): Flow<Result<VerifiableCredentialWithDisplaysAndClusters?, CredentialWithDisplaysRepositoryError>> =
        daoFlow.flatMapLatest { dao ->
            dao?.getNullableVerifiableCredentialWithDisplaysAndClustersFlowById(credentialId)
                ?.catchAndMap { throwable ->
                    throwable.toVerifiableCredentialWithDisplaysAndClustersRepositoryError("Error to get CredentialWithDisplaysAndClusters")
                } ?: emptyFlow()
        }

    override fun getVerifiableCredentialsWithDisplaysAndClustersFlow():
        Flow<Result<List<VerifiableCredentialWithDisplaysAndClusters>, CredentialWithDisplaysRepositoryError>> =
        daoFlow.flatMapLatest { dao ->
            dao?.getVerifiableCredentialsWithDisplaysAndClustersFlow()
                ?.catchAndMap { throwable ->
                    throwable.toVerifiableCredentialWithDisplaysAndClustersRepositoryError("Error to get CredentialWithDisplaysAndClusters")
                } ?: emptyFlow()
        }

    private val daoFlow = daoProvider.verifiableCredentialWithDisplaysAndClustersDaoFlow
}
