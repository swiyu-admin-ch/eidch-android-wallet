package ch.admin.foitt.wallet.feature.credentialOffer.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.credentialOffer.domain.model.CredentialOffer
import ch.admin.foitt.wallet.feature.credentialOffer.domain.model.GetCredentialOfferFlowError
import ch.admin.foitt.wallet.feature.credentialOffer.domain.model.toGetCredentialOfferFlowError
import ch.admin.foitt.wallet.feature.credentialOffer.domain.usecase.GetCredentialOfferFlow
import ch.admin.foitt.wallet.platform.credential.domain.model.MapToCredentialDisplayDataError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.MapToCredentialDisplayData
import ch.admin.foitt.wallet.platform.credentialCluster.domain.usercase.MapToCredentialClaimCluster
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialWithDisplaysRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialWithDisplaysAndClustersRepository
import ch.admin.foitt.wallet.platform.utils.andThen
import ch.admin.foitt.wallet.platform.utils.mapError
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCredentialOfferFlowImpl @Inject constructor(
    private val verifiableCredentialWithDisplaysAndClustersRepository: VerifiableCredentialWithDisplaysAndClustersRepository,
    private val mapToCredentialDisplayData: MapToCredentialDisplayData,
    private val mapToCredentialClaimCluster: MapToCredentialClaimCluster,
) : GetCredentialOfferFlow {
    override fun invoke(credentialId: Long): Flow<Result<CredentialOffer?, GetCredentialOfferFlowError>> =
        verifiableCredentialWithDisplaysAndClustersRepository.getNullableVerifiableCredentialWithDisplaysAndClustersFlowById(
            credentialId
        ).mapError(CredentialWithDisplaysRepositoryError::toGetCredentialOfferFlowError)
            .andThen { credentialWithDisplaysAndClusters ->
                coroutineBinding {
                    credentialWithDisplaysAndClusters?.let { credentialWithDisplaysAndClusters ->
                        val credentialDisplayData = mapToCredentialDisplayData(
                            verifiableCredential = credentialWithDisplaysAndClusters.verifiableCredential,
                            credentialDisplays = credentialWithDisplaysAndClusters.credentialDisplays,
                            claims = credentialWithDisplaysAndClusters.clusters.flatMap { it.claimsWithDisplays }
                        ).mapError(MapToCredentialDisplayDataError::toGetCredentialOfferFlowError)
                            .bind()

                        CredentialOffer(
                            credential = credentialDisplayData,
                            claims = mapToCredentialClaimCluster(credentialWithDisplaysAndClusters.clusters)
                        )
                    }
                }
            }
}
