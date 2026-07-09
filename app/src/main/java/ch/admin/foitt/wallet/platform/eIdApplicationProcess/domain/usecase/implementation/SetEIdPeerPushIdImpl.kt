package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestationPoP
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.GenerateProofOfPossessionError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.RequestClientAttestationError
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.GenerateProofOfPossession
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.RequestClientAttestation
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdPeerPushIdRequest
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.SIdRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.SetEIdPeerPushIdError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.toSetEIdPeerPushIdError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.SIdRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.SetEIdPeerPushId
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import ch.admin.foitt.wallet.platform.utils.SafeJson
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class SetEIdPeerPushIdImpl @Inject constructor(
    private val sIdRepository: SIdRepository,
    private val requestClientAttestation: RequestClientAttestation,
    private val generateProofOfPossession: GenerateProofOfPossession,
    private val environmentSetupRepository: EnvironmentSetupRepository,
    private val safeJson: SafeJson,
) : SetEIdPeerPushId {
    override suspend fun invoke(caseId: String, pushId: String): Result<Unit, SetEIdPeerPushIdError> = coroutineBinding {
        val clientAttestation = requestClientAttestation()
            .mapError(RequestClientAttestationError::toSetEIdPeerPushIdError).bind()

        val challengeResponse = sIdRepository.fetchChallenge()
            .mapError(SIdRepositoryError::toSetEIdPeerPushIdError).bind()

        val request = EIdPeerPushIdRequest(pushId)
        val requestBody = safeJson.safeEncodeObjectToJsonElement(request)
            .mapError(JsonParsingError::toSetEIdPeerPushIdError).bind()

        val clientAttestationPoP: ClientAttestationPoP = generateProofOfPossession(
            clientAttestation = clientAttestation,
            challenge = challengeResponse.challenge,
            audience = environmentSetupRepository.sidBackendUrl,
            requestBody = requestBody
        ).mapError(GenerateProofOfPossessionError::toSetEIdPeerPushIdError).bind()

        sIdRepository.setPeerPushId(
            caseId = caseId,
            clientAttestation = clientAttestation,
            clientAttestationPoP = clientAttestationPoP,
            request = request
        ).mapError(SIdRepositoryError::toSetEIdPeerPushIdError).bind()
    }
}
