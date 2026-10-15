package ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestationPoP
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.GenerateProofOfPossessionError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.RequestClientAttestationError
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.GenerateProofOfPossession
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.RequestClientAttestation
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceRepositoryError
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceRequest
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.SendNonComplianceReportError
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.toSendNonComplianceReportError
import ch.admin.foitt.wallet.platform.nonCompliance.domain.repository.NonComplianceRepository
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.SubmitNonComplianceReport
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import ch.admin.foitt.wallet.platform.utils.SafeJson
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class SubmitNonComplianceReportImpl @Inject constructor(
    private val requestClientAttestation: RequestClientAttestation,
    private val nonComplianceRepository: NonComplianceRepository,
    private val safeJson: SafeJson,
    private val generateProofOfPossession: GenerateProofOfPossession,
    private val environmentSetupRepository: EnvironmentSetupRepository,
) : SubmitNonComplianceReport {
    override suspend fun invoke(request: NonComplianceRequest): Result<Unit, SendNonComplianceReportError> = coroutineBinding {
        val clientAttestation: ClientAttestation = requestClientAttestation()
            .mapError(RequestClientAttestationError::toSendNonComplianceReportError)
            .bind()

        val challengeResponse = nonComplianceRepository.fetchChallenge()
            .mapError(NonComplianceRepositoryError::toSendNonComplianceReportError)
            .bind()

        val requestBody = safeJson.safeEncodeObjectToJsonElement(request)
            .mapError(JsonParsingError::toSendNonComplianceReportError)
            .bind()

        val clientAttestationProofOfPossession: ClientAttestationPoP = generateProofOfPossession(
            clientAttestation = clientAttestation,
            challenge = challengeResponse.challenge,
            audience = environmentSetupRepository.nonComplianceBaseUrl,
            requestBody = requestBody,
        ).mapError(GenerateProofOfPossessionError::toSendNonComplianceReportError)
            .bind()

        nonComplianceRepository.sendReport(
            clientAttestation = clientAttestation,
            clientAttestationPoP = clientAttestationProofOfPossession,
            nonComplianceRequest = request,
        ).mapError(NonComplianceRepositoryError::toSendNonComplianceReportError)
            .bind()
    }
}
