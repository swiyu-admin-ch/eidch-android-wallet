package ch.admin.foitt.wallet.platform.pushNotification.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.appAttestation.domain.model.GenerateProofOfPossessionError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.RequestClientAttestationError
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.GenerateProofOfPossession
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.RequestClientAttestation
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.FetchPushChallengeError
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.GeneratePushClientAttestationError
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.PushClientAttestation
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.toGeneratePushClientAttestationError
import ch.admin.foitt.wallet.platform.pushNotification.domain.repository.PushNotificationRepository
import ch.admin.foitt.wallet.platform.pushNotification.domain.usecase.GeneratePushClientAttestation
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import kotlinx.serialization.json.JsonElement
import javax.inject.Inject

class GeneratePushClientAttestationImpl @Inject constructor(
    private val pushNotificationRepository: PushNotificationRepository,
    private val requestClientAttestation: RequestClientAttestation,
    private val generateProofOfPossession: GenerateProofOfPossession,
    private val environmentSetupRepository: EnvironmentSetupRepository,
) : GeneratePushClientAttestation {
    override suspend fun invoke(
        requestBody: JsonElement
    ): Result<PushClientAttestation, GeneratePushClientAttestationError> = coroutineBinding {
        val challengeResponse = pushNotificationRepository.fetchPushChallenge()
            .mapError(FetchPushChallengeError::toGeneratePushClientAttestationError).bind()

        val clientAttestation = requestClientAttestation()
            .mapError(RequestClientAttestationError::toGeneratePushClientAttestationError).bind()

        val clientAttestationPoP = generateProofOfPossession(
            clientAttestation = clientAttestation,
            challenge = challengeResponse.nonce,
            audience = environmentSetupRepository.notificationBackendUrl,
            requestBody = requestBody
        ).mapError(GenerateProofOfPossessionError::toGeneratePushClientAttestationError).bind()

        PushClientAttestation(
            attestation = clientAttestation,
            pop = clientAttestationPoP
        )
    }
}
