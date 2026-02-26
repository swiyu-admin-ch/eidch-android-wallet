package ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialDisplayData
import ch.admin.foitt.wallet.platform.credential.domain.model.MapToCredentialDisplayDataError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.MapToCredentialDisplayData
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialWithDisplaysAndClusters
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialWithDisplaysRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.GetCredentialsWithDetailsFlowError
import ch.admin.foitt.wallet.platform.ssi.domain.model.toGetCredentialsWithDetailsFlowError
import ch.admin.foitt.wallet.platform.ssi.domain.model.toGetCredentialsWithDisplaysFlowError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialWithDisplaysAndClustersRepository
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetCredentialsWithDetailsFlow
import ch.admin.foitt.wallet.platform.utils.andThen
import ch.admin.foitt.wallet.platform.utils.mapError
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCredentialsWithDetailsFlowImpl @Inject constructor(
    private val verifiableCredentialWithDisplaysAndClustersRepository: VerifiableCredentialWithDisplaysAndClustersRepository,
    private val mapToCredentialDisplayData: MapToCredentialDisplayData,
) : GetCredentialsWithDetailsFlow {
    override fun invoke(): Flow<Result<List<CredentialDisplayData>, GetCredentialsWithDetailsFlowError>> =
        verifiableCredentialWithDisplaysAndClustersRepository.getVerifiableCredentialsWithDisplaysAndClustersFlow()
            .mapError(CredentialWithDisplaysRepositoryError::toGetCredentialsWithDetailsFlowError)
            .andThen { credentials ->
                coroutineBinding {
                    createCredentialDisplayData(
                        credentials = credentials,
                    ).bind()
                }
            }

    private suspend fun createCredentialDisplayData(
        credentials: List<VerifiableCredentialWithDisplaysAndClusters>
    ): Result<List<CredentialDisplayData>, GetCredentialsWithDetailsFlowError> = coroutineBinding {
        credentials.map { verifiableCredentialWithDetail ->
            val cluster = verifiableCredentialWithDetail.clusters.first()

            val credentialDisplayData = mapToCredentialDisplayData(
                verifiableCredential = verifiableCredentialWithDetail.verifiableCredential,
                credentialDisplays = verifiableCredentialWithDetail.credentialDisplays,
                claims = cluster.claimsWithDisplays
            ).mapError(MapToCredentialDisplayDataError::toGetCredentialsWithDisplaysFlowError)
                .bind()

            credentialDisplayData
        }
    }
}
