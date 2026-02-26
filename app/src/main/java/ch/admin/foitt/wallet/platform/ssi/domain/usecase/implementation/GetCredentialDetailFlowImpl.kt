package ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.credential.domain.model.MapToCredentialDisplayDataError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.MapToCredentialDisplayData
import ch.admin.foitt.wallet.platform.credentialCluster.domain.usercase.MapToCredentialClaimCluster
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialDetail
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialWithDisplaysRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.GetCredentialDetailFlowError
import ch.admin.foitt.wallet.platform.ssi.domain.model.toGetCredentialDetailFlowError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialWithDisplaysAndClustersRepository
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetCredentialDetailFlow
import ch.admin.foitt.wallet.platform.utils.andThen
import ch.admin.foitt.wallet.platform.utils.mapError
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCredentialDetailFlowImpl @Inject constructor(
    private val verifiableCredentialWithDisplaysAndClustersRepository: VerifiableCredentialWithDisplaysAndClustersRepository,
    private val mapToCredentialDisplayData: MapToCredentialDisplayData,
    private val mapToCredentialClaimCluster: MapToCredentialClaimCluster
) : GetCredentialDetailFlow {
    override fun invoke(credentialId: Long): Flow<Result<CredentialDetail?, GetCredentialDetailFlowError>> =
        verifiableCredentialWithDisplaysAndClustersRepository.getNullableVerifiableCredentialWithDisplaysAndClustersFlowById(credentialId)
            .mapError(CredentialWithDisplaysRepositoryError::toGetCredentialDetailFlowError)
            .andThen { credentialWithDisplaysAndClusters ->
                coroutineBinding {
                    credentialWithDisplaysAndClusters?.let { credentialWithDisplaysAndClusters ->
                        val credentialDisplayData = mapToCredentialDisplayData(
                            verifiableCredential = credentialWithDisplaysAndClusters.verifiableCredential,
                            credentialDisplays = credentialWithDisplaysAndClusters.credentialDisplays,
                            claims = credentialWithDisplaysAndClusters.clusters.flatMap { it.claimsWithDisplays }
                        ).mapError(MapToCredentialDisplayDataError::toGetCredentialDetailFlowError)
                            .bind()

                        CredentialDetail(
                            credential = credentialDisplayData,
                            clusterItems = mapToCredentialClaimCluster(credentialWithDisplaysAndClusters.clusters)
                        )
                    }
                }
            }
}
