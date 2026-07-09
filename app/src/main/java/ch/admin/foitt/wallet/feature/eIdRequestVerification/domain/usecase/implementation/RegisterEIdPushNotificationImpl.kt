package ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.RegisterEIdPushNotificationError
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.toRegisterEIdPushNotificationError
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.RegisterEIdPushNotification
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.SetEIdPeerPushIdError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestCaseRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.SetEIdPeerPushId
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.FetchPushDeviceTokenError
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.GeneratePushClientAttestationError
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.PushRegistrationRequest
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.RegisterPushDeviceTokenError
import ch.admin.foitt.wallet.platform.pushNotification.domain.repository.PushDeviceTokenRepository
import ch.admin.foitt.wallet.platform.pushNotification.domain.repository.PushNotificationRepository
import ch.admin.foitt.wallet.platform.pushNotification.domain.usecase.GeneratePushClientAttestation
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import ch.admin.foitt.wallet.platform.utils.SafeJson
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class RegisterEIdPushNotificationImpl @Inject constructor(
    private val pushDeviceTokenRepository: PushDeviceTokenRepository,
    private val pushNotificationRepository: PushNotificationRepository,
    private val eIdRequestCaseRepository: EIdRequestCaseRepository,
    private val generatePushClientAttestation: GeneratePushClientAttestation,
    private val safeJson: SafeJson,
    private val setEIdPeerPushId: SetEIdPeerPushId,
) : RegisterEIdPushNotification {
    override suspend fun invoke(caseId: String): Result<Unit, RegisterEIdPushNotificationError> =
        coroutineBinding {
            val pushDeviceToken = pushDeviceTokenRepository.fetchToken()
                .mapError(FetchPushDeviceTokenError::toRegisterEIdPushNotificationError).bind()

            val request = PushRegistrationRequest(
                deviceToken = pushDeviceToken,
                platform = PLATFORM,
            )
            val requestBody = safeJson.safeEncodeObjectToJsonElement(request)
                .mapError(JsonParsingError::toRegisterEIdPushNotificationError).bind()

            val pushClientAttestation = generatePushClientAttestation(requestBody)
                .mapError(GeneratePushClientAttestationError::toRegisterEIdPushNotificationError)
                .bind()

            val pushIdResponse = pushNotificationRepository.registerPushDeviceToken(
                pushClientAttestation.attestation,
                pushClientAttestation.pop,
                request
            ).mapError(RegisterPushDeviceTokenError::toRegisterEIdPushNotificationError).bind()

            eIdRequestCaseRepository.setPushId(caseId = caseId, pushId = pushIdResponse.pushId)
                .mapError(EIdRequestCaseRepositoryError::toRegisterEIdPushNotificationError).bind()

            setEIdPeerPushId(
                caseId = caseId,
                pushId = pushIdResponse.pushId
            ).mapError(SetEIdPeerPushIdError::toRegisterEIdPushNotificationError).bind()
        }

    companion object {
        private const val PLATFORM = "android"
    }
}
