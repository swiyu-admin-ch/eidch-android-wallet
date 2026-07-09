package ch.admin.foitt.wallet.platform.pushNotification.data

import ch.admin.foitt.openid4vc.di.OpenId4VcModule.Companion.NAMED_DEFAULT_HTTP_CLIENT
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestationPoP
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.DeletePushIdError
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.FetchPushChallengeError
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.PushChallengeResponse
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.PushRegistrationRequest
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.PushRegistrationResponse
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.PushUpdateTokenRequest
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.RegisterPushDeviceTokenError
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.UpdatePushDeviceTokenError
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.toDeletePushIdError
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.toFetchPushChallengeError
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.toRegisterPushDeviceTokenError
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.toUpdatePushDeviceTokenError
import ch.admin.foitt.wallet.platform.pushNotification.domain.repository.PushNotificationRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject
import javax.inject.Named

class PushNotificationRepositoryImpl @Inject constructor(
    @param:Named(NAMED_DEFAULT_HTTP_CLIENT) private val httpClient: HttpClient,
    private val environmentSetupRepo: EnvironmentSetupRepository,
) : PushNotificationRepository {

    override suspend fun fetchPushChallenge(): Result<PushChallengeResponse, FetchPushChallengeError> =
        runSuspendCatching<PushChallengeResponse> {
            httpClient.get(environmentSetupRepo.notificationBackendUrl + "/targets/challenge").body()
        }.mapError { it.toFetchPushChallengeError("Fetch push challenge failed") }

    override suspend fun registerPushDeviceToken(
        clientAttestation: ClientAttestation,
        clientAttestationPoP: ClientAttestationPoP,
        request: PushRegistrationRequest
    ): Result<PushRegistrationResponse, RegisterPushDeviceTokenError> =
        runSuspendCatching<PushRegistrationResponse> {
            httpClient.post(environmentSetupRepo.notificationBackendUrl + "/targets") {
                header(ClientAttestation.REQUEST_HEADER, clientAttestation.attestation.rawJwt)
                header(ClientAttestationPoP.REQUEST_HEADER, clientAttestationPoP.value)
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        }.mapError { it.toRegisterPushDeviceTokenError("Registering push token failed") }

    override suspend fun updatePushDeviceToken(
        clientAttestation: ClientAttestation,
        clientAttestationPoP: ClientAttestationPoP,
        request: PushUpdateTokenRequest,
    ): Result<Unit, UpdatePushDeviceTokenError> = runSuspendCatching<Unit> {
        httpClient.patch(environmentSetupRepo.notificationBackendUrl + "/targets") {
            header(ClientAttestation.REQUEST_HEADER, clientAttestation.attestation.rawJwt)
            header(ClientAttestationPoP.REQUEST_HEADER, clientAttestationPoP.value)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }.mapError { it.toUpdatePushDeviceTokenError("Updating push token failed") }

    override suspend fun deletePushId(
        clientAttestation: ClientAttestation,
        clientAttestationPoP: ClientAttestationPoP,
        pushId: String
    ): Result<Unit, DeletePushIdError> = runSuspendCatching<Unit> {
        httpClient.delete(environmentSetupRepo.notificationBackendUrl + "/targets/$pushId") {
            header(ClientAttestation.REQUEST_HEADER, clientAttestation.attestation.rawJwt)
            header(ClientAttestationPoP.REQUEST_HEADER, clientAttestationPoP.value)
        }.body()
    }.mapError { it.toDeletePushIdError("Deleting push ID failed") }
}
