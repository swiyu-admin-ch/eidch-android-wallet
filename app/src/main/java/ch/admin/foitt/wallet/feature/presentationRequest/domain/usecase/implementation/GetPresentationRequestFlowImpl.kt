package ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.GetPresentationRequestFlowError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.PresentationRequestDisplayData
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.toGetPresentationRequestFlowError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.GetPresentationRequestFlow
import ch.admin.foitt.wallet.platform.credential.domain.model.MapToCredentialDisplayDataError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.MapToCredentialDisplayData
import ch.admin.foitt.wallet.platform.credentialCluster.domain.usercase.MapToCredentialClaimCluster
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestField
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
        requestedFields: List<PresentationRequestField>,
    ): Flow<Result<PresentationRequestDisplayData, GetPresentationRequestFlowError>> =
        verifiableCredentialWithDisplaysAndClustersRepository.getVerifiableCredentialWithDisplaysAndClustersFlowById(id)
            .mapError(CredentialWithDisplaysRepositoryError::toGetPresentationRequestFlowError)
            .andThen { credentialWithDisplaysAndClusters ->
                coroutineBinding {
                    val credentialDisplayData = mapToCredentialDisplayData(
                        verifiableCredential = credentialWithDisplaysAndClusters.verifiableCredential,
                        credentialDisplays = credentialWithDisplaysAndClusters.credentialDisplays,
                        claims = credentialWithDisplaysAndClusters.clusters.flatMap { it.claimsWithDisplays }
                    ).mapError(MapToCredentialDisplayDataError::toGetPresentationRequestFlowError)
                        .bind()

                    PresentationRequestDisplayData(
                        credential = credentialDisplayData,
                        requestedClaims = mapToCredentialClaimCluster(
                            credentialWithDisplaysAndClusters.clusters.filterFor(requestedFields)
                        )
                    )
                }
            }

    private fun List<ClusterWithDisplaysAndClaims>.filterFor(
        requestedFields: List<PresentationRequestField>
    ): List<ClusterWithDisplaysAndClaims> = this.map { cluster ->
        val filteredClaimsWithDisplays = cluster.claimsWithDisplays.filter { (claim, _) ->
            requestedFields.any { field ->
                field.key == claim.key
            }
        }

        cluster.copy(
            claimsWithDisplays = filteredClaimsWithDisplays
        )
    }
}
