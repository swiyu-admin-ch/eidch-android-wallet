package ch.admin.foitt.wallet.platform.pushNotification.domain.repository

import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestationPoP
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.DeletePushIdError
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.FetchPushChallengeError
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.PushChallengeResponse
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.PushRegistrationRequest
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.PushRegistrationResponse
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.PushUpdateTokenRequest
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.RegisterPushDeviceTokenError
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.UpdatePushDeviceTokenError
import com.github.michaelbull.result.Result

interface PushNotificationRepository {
    suspend fun fetchPushChallenge(): Result<PushChallengeResponse, FetchPushChallengeError>

    /**
     * Registers device push notification token and returns a push id token.
     */
    suspend fun registerPushDeviceToken(
        clientAttestation: ClientAttestation,
        clientAttestationPoP: ClientAttestationPoP,
        request: PushRegistrationRequest
    ): Result<PushRegistrationResponse, RegisterPushDeviceTokenError>

    suspend fun updatePushDeviceToken(
        clientAttestation: ClientAttestation,
        clientAttestationPoP: ClientAttestationPoP,
        request: PushUpdateTokenRequest,
    ): Result<Unit, UpdatePushDeviceTokenError>

    suspend fun deletePushId(
        clientAttestation: ClientAttestation,
        clientAttestationPoP: ClientAttestationPoP,
        pushId: String
    ): Result<Unit, DeletePushIdError>
}
