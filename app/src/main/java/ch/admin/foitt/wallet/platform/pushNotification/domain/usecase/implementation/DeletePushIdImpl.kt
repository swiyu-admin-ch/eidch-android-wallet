package ch.admin.foitt.wallet.platform.pushNotification.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.pushNotification.domain.model.DeletePushIdError
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.GeneratePushClientAttestationError
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.toDeletePushIdError
import ch.admin.foitt.wallet.platform.pushNotification.domain.repository.PushNotificationRepository
import ch.admin.foitt.wallet.platform.pushNotification.domain.usecase.DeletePushId
import ch.admin.foitt.wallet.platform.pushNotification.domain.usecase.GeneratePushClientAttestation
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import kotlinx.serialization.json.JsonObject
import javax.inject.Inject

class DeletePushIdImpl @Inject constructor(
    private val pushNotificationRepository: PushNotificationRepository,
    private val generatePushClientAttestation: GeneratePushClientAttestation,
) : DeletePushId {
    override suspend fun invoke(pushId: String): Result<Unit, DeletePushIdError> = coroutineBinding {
        val pushClientAttestation = generatePushClientAttestation(JsonObject(emptyMap()))
            .mapError(GeneratePushClientAttestationError::toDeletePushIdError).bind()

        pushNotificationRepository.deletePushId(
            pushClientAttestation.attestation,
            pushClientAttestation.pop,
            pushId
        ).bind()
    }
}
