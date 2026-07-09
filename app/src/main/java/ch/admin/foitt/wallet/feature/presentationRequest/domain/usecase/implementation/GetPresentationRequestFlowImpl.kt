package ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointer
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.claimsPathPointerFrom
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.pointsAtSetOf
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.GetPresentationRequestFlowError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.PresentationRequestDisplayData
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.toGetPresentationRequestFlowError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.GetPresentationRequestFlow
import ch.admin.foitt.wallet.platform.credential.domain.model.MapToCredentialDisplayDataError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.MapToCredentialDisplayData
import ch.admin.foitt.wallet.platform.credentialCluster.domain.usercase.MapToCredentialClaimCluster
import ch.admin.foitt.wallet.platform.database.domain.model.ClusterWithDisplaysAndClaims
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialWithDisplaysRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialWithDisplaysAndClustersRepository
import ch.admin.foitt.wallet.platform.utils.andThen
import ch.admin.foitt.wallet.platform.utils.mapError
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPresentationRequestFlowImpl @Inject constructor(
    private val verifiableCredentialWithDisplaysAndClustersRepository: VerifiableCredentialWithDisplaysAndClustersRepository,
    private val mapToCredentialDisplayData: MapToCredentialDisplayData,
    private val mapToCredentialClaimCluster: MapToCredentialClaimCluster
) : GetPresentationRequestFlow {
    override fun invoke(
        id: Long,
        presentationPaths: List<ClaimsPathPointer>,
    ): Flow<Result<PresentationRequestDisplayData, GetPresentationRequestFlowError>> =
        verifiableCredentialWithDisplaysAndClustersRepository.getVerifiableCredentialWithDisplaysAndClustersFlowById(id)
            .mapError(CredentialWithDisplaysRepositoryError::toGetPresentationRequestFlowError)
            .andThen { credentialWithDisplaysAndClusters ->
                coroutineBinding {
                    val credentialDisplayData = mapToCredentialDisplayData(
                        verifiableCredential = credentialWithDisplaysAndClusters.verifiableCredential,
                        credentialDisplays = credentialWithDisplaysAndClusters.credentialDisplays,
                        claims = credentialWithDisplaysAndClusters.clusters.flatMap { it.claimsWithDisplays },
                        credentialFormat = credentialWithDisplaysAndClusters.credential.format
                    ).mapError(MapToCredentialDisplayDataError::toGetPresentationRequestFlowError)
                        .bind()

                    PresentationRequestDisplayData(
                        credential = credentialDisplayData,
                        requestedClaims = mapToCredentialClaimCluster(
                            credentialWithDisplaysAndClusters.clusters.filterFor(presentationPaths)
                        )
                    )
                }
            }

    private fun List<ClusterWithDisplaysAndClaims>.filterFor(
        presentationPaths: List<ClaimsPathPointer>
    ): List<ClusterWithDisplaysAndClaims> = this.map { cluster ->
        if (
            presentationPaths.any {
                val clusterPath = claimsPathPointerFrom(cluster.clusterWithDisplays.cluster.path) ?: return@any false
                it.pointsAtSetOf(clusterPath)
            }
        ) {
            return@map cluster
        }

        val filteredClaimsWithDisplays = cluster.claimsWithDisplays.filter { (claim, _) ->
            presentationPaths.any { requestedPath ->
                val claimPath = claimsPathPointerFrom(claim.path) ?: return@any false
                requestedPath.pointsAtSetOf(claimPath)
            }
        }

        cluster.copy(
            claimsWithDisplays = filteredClaimsWithDisplays
        )
    }
}
