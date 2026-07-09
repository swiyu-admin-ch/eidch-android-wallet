package ch.admin.foitt.wallet.platform.pushNotification.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestCaseRepository
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.FetchPushDeviceTokenError
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.GeneratePushClientAttestationError
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.PushUpdateTokenRequest
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.UpdatePushDeviceTokenError
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.toUpdatePushDeviceTokenError
import ch.admin.foitt.wallet.platform.pushNotification.domain.repository.PushDeviceTokenRepository
import ch.admin.foitt.wallet.platform.pushNotification.domain.repository.PushNotificationRepository
import ch.admin.foitt.wallet.platform.pushNotification.domain.usecase.GeneratePushClientAttestation
import ch.admin.foitt.wallet.platform.pushNotification.domain.usecase.UpdatePushToken
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import ch.admin.foitt.wallet.platform.utils.SafeJson
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class UpdatePushTokenImpl @Inject constructor(
    private val eIdRequestCaseRepository: EIdRequestCaseRepository,
    private val pushDeviceTokenRepository: PushDeviceTokenRepository,
    private val pushNotificationRepository: PushNotificationRepository,
    private val generatePushClientAttestation: GeneratePushClientAttestation,
    private val safeJson: SafeJson,
) : UpdatePushToken {
    override suspend fun invoke(): Result<Unit, UpdatePushDeviceTokenError> = coroutineBinding {
        val pushDeviceToken = pushDeviceTokenRepository.fetchToken()
            .mapError(FetchPushDeviceTokenError::toUpdatePushDeviceTokenError).bind()

        val cases = eIdRequestCaseRepository.getEIdRequestCasesWithPushId()
            .mapError(EIdRequestCaseRepositoryError::toUpdatePushDeviceTokenError).bind()

        if (cases.isEmpty()) {
            return@coroutineBinding
        }
        val pushIds = cases.map { it.pushId!! }.toList()

        val request = PushUpdateTokenRequest(
            pushIds = pushIds,
            pushDeviceToken = pushDeviceToken
        )
        val requestBody = safeJson.safeEncodeObjectToJsonElement(request)
            .mapError(JsonParsingError::toUpdatePushDeviceTokenError).bind()

        val pushClientAttestation = generatePushClientAttestation(requestBody)
            .mapError(GeneratePushClientAttestationError::toUpdatePushDeviceTokenError).bind()

        pushNotificationRepository.updatePushDeviceToken(
            pushClientAttestation.attestation,
            pushClientAttestation.pop,
            request
        ).bind()
    }
}
