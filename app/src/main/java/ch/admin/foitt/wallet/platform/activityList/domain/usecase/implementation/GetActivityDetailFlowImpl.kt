package ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityDetail
import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityListError
import ch.admin.foitt.wallet.platform.activityList.domain.model.GetActivityDetailFlowError
import ch.admin.foitt.wallet.platform.activityList.domain.model.toGetActivityDetailFlowError
import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityWithDetailsRepository
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.GetActivityDetailFlow
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.MapToActivityDisplayData
import ch.admin.foitt.wallet.platform.credential.domain.model.MapToCredentialDisplayDataError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.MapToCredentialDisplayData
import ch.admin.foitt.wallet.platform.credentialCluster.domain.usercase.MapToCredentialClaimCluster
import ch.admin.foitt.wallet.platform.database.domain.model.ActivityClaimEntity
import ch.admin.foitt.wallet.platform.database.domain.model.ClusterWithDisplaysAndClaims
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialWithDisplaysAndClustersRepository
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetActivityDetailFlowImpl @Inject constructor(
    private val verifiableCredentialWithDisplaysAndClustersRepository: VerifiableCredentialWithDisplaysAndClustersRepository,
    private val activityWithDetailsRepository: ActivityWithDetailsRepository,
    private val mapToActivityDisplayData: MapToActivityDisplayData,
    private val mapToCredentialDisplayData: MapToCredentialDisplayData,
    private val mapToCredentialClaimCluster: MapToCredentialClaimCluster,
) : GetActivityDetailFlow {
    override fun invoke(
        credentialId: Long,
        activityId: Long,
    ): Flow<Result<ActivityDetail?, GetActivityDetailFlowError>> = combine(
        verifiableCredentialWithDisplaysAndClustersRepository.getVerifiableCredentialWithDisplaysAndClustersFlowById(credentialId),
        activityWithDetailsRepository.getNullableByIdFlow(activityId)
    ) { verifiableCredentialResult, activityWithDisplaysResult ->
        val credentialWithDisplaysAndClusters = verifiableCredentialResult.getOrElse {
            return@combine Err(ActivityListError.Unexpected(IllegalStateException("Error getting Credential with Displays and Clusters")))
        }
        val activityWithDisplays = activityWithDisplaysResult.getOrElse {
            return@combine Err(ActivityListError.Unexpected(IllegalStateException("Error getting Activity with Displays")))
        }

        coroutineBinding {
            activityWithDisplays?.let { activityWithDetails ->
                val activityDisplayData = mapToActivityDisplayData(activityWithDetails)
                val credentialDisplayData = mapToCredentialDisplayData(
                    verifiableCredential = credentialWithDisplaysAndClusters.verifiableCredential,
                    credentialDisplays = credentialWithDisplaysAndClusters.credentialDisplays,
                    claims = credentialWithDisplaysAndClusters.clusters.flatMap { it.claimsWithDisplays }
                ).mapError(MapToCredentialDisplayDataError::toGetActivityDetailFlowError)
                    .bind()

                val claims = mapToCredentialClaimCluster(
                    credentialWithDisplaysAndClusters.clusters.filterFor(activityWithDetails.claims)
                )

                ActivityDetail(
                    activity = activityDisplayData,
                    credential = credentialDisplayData,
                    claims = claims,
                )
            }
        }
    }

    private fun List<ClusterWithDisplaysAndClaims>.filterFor(
        activityClaims: List<ActivityClaimEntity>,
    ): List<ClusterWithDisplaysAndClaims> = this.map { cluster ->
        val filteredClaimsWithDisplays = cluster.claimsWithDisplays.filter { (claim, _) ->
            activityClaims.any { activityClaim ->
                activityClaim.claimId == claim.id
            }
        }

        cluster.copy(
            claimsWithDisplays = filteredClaimsWithDisplays
        )
    }
}
