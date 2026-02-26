package ch.admin.foitt.wallet.platform.nonCompliance.domain.repository

import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestationPoP
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceChallengeResponse
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceRepositoryError
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceRequest
import com.github.michaelbull.result.Result

interface NonComplianceRepository {
    suspend fun fetchChallenge(): Result<NonComplianceChallengeResponse, NonComplianceRepositoryError>
    suspend fun sendReport(
        clientAttestation: ClientAttestation,
        clientAttestationPoP: ClientAttestationPoP,
        nonComplianceRequest: NonComplianceRequest,
    ): Result<Unit, NonComplianceRepositoryError>
}
