package ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.wallet.platform.activityList.domain.model.CredentialActivityRepositoryError
import ch.admin.foitt.wallet.platform.activityList.domain.repository.CredentialActivityRepository
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestationPoP
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.GenerateProofOfPossessionError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.RequestClientAttestationError
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.GenerateProofOfPossession
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.RequestClientAttestation
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceError
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceMetadata
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceReportReason
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceRepositoryError
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceRequest
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceRequestField
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.SendNonComplianceReportError
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.toSendNonComplianceReportError
import ch.admin.foitt.wallet.platform.nonCompliance.domain.repository.NonComplianceRepository
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.SendNonComplianceReport
import ch.admin.foitt.wallet.platform.ssi.domain.model.VerifiableCredentialRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialRepository
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import ch.admin.foitt.wallet.platform.utils.SafeJson
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapBoth
import com.github.michaelbull.result.mapError
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class SendNonComplianceReportImpl @Inject constructor(
    private val credentialActivityRepository: CredentialActivityRepository,
    private val verifiableCredentialRepo: VerifiableCredentialRepository,
    private val requestClientAttestation: RequestClientAttestation,
    private val nonComplianceRepository: NonComplianceRepository,
    private val safeJson: SafeJson,
    private val generateProofOfPossession: GenerateProofOfPossession,
    private val environmentSetupRepository: EnvironmentSetupRepository,
) : SendNonComplianceReport {
    override suspend fun invoke(
        activityId: Long,
        reportReason: NonComplianceReportReason,
        description: String,
        email: String?,
    ): Result<Unit, SendNonComplianceReportError> = coroutineBinding {
        val activity = credentialActivityRepository.getById(activityId)
            .mapError(CredentialActivityRepositoryError::toSendNonComplianceReportError)
            .bind()

        val credential = verifiableCredentialRepo.getById(activity.credentialId)
            .mapError(VerifiableCredentialRepositoryError::toSendNonComplianceReportError)
            .bind()

        val presentationRequest = getPresentationRequest(activity.nonComplianceData).bind()
        val createdAt = Instant.ofEpochSecond(activity.createdAt)
        val formattedCreatedAt = DateTimeFormatter.ISO_INSTANT.format(createdAt)
        val presentationRequestFields = getPresentationRequestFields(presentationRequest)

        val nonComplianceMetadata = NonComplianceMetadata(
            verifierDid = presentationRequest.clientId,
            verifierUrl = presentationRequest.responseUri,
            presentationActionCreatedAt = formattedCreatedAt,
            presentedCredentialIssuerDid = credential.issuer,
            presentationRequestJwt = activity.nonComplianceData,
            presentationRequestFields = presentationRequestFields ?: emptyList(),
        )

        val nonComplianceRequest = NonComplianceRequest(
            type = reportReason.type,
            description = description,
            email = email,
            metadata = nonComplianceMetadata
        )

        val clientAttestation: ClientAttestation = requestClientAttestation()
            .mapError(RequestClientAttestationError::toSendNonComplianceReportError)
            .bind()

        val challengeResponse = nonComplianceRepository.fetchChallenge()
            .mapError(NonComplianceRepositoryError::toSendNonComplianceReportError)
            .bind()

        val requestBody = safeJson.safeEncodeObjectToJsonElement(nonComplianceRequest)
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
            nonComplianceRequest = nonComplianceRequest,
        ).mapError(NonComplianceRepositoryError::toSendNonComplianceReportError)
            .bind()
    }

    private fun getPresentationRequest(nonComplianceData: String?): Result<PresentationRequest, SendNonComplianceReportError> = binding {
        if (nonComplianceData == null) {
            return@binding Err(
                NonComplianceError.Unexpected(IllegalStateException("nonComplianceData must not be null"))
            ).bind<PresentationRequest>()
        }
        val presentationRequest = runSuspendCatching {
            Jwt(nonComplianceData)
        }.mapBoth(
            success = { jwt ->
                safeJson.safeDecodeElementTo<PresentationRequest>(jwt.payloadJson)
                    .mapError(JsonParsingError::toSendNonComplianceReportError)
                    .bind()
            },
            failure = {
                safeJson.safeDecodeStringTo<PresentationRequest>(nonComplianceData)
                    .mapError(JsonParsingError::toSendNonComplianceReportError)
                    .bind()
            },
        )

        presentationRequest
    }

    private fun getPresentationRequestFields(presentationRequest: PresentationRequest) =
        presentationRequest.presentationDefinition.inputDescriptors.firstOrNull()?.constraints?.fields?.map { (filter, path) ->
            val constraint = filter?.const
            val name = path.joinToString(", ")

            NonComplianceRequestField(
                name = name,
                constraint = constraint,
            )
        }
}
