package ch.admin.foitt.wallet.platform.appAttestation.data.repository

import ch.admin.foitt.openid4vc.di.ExternalOpenId4VcModule.Companion.NAMED_DEFAULT_HTTP_CLIENT
import ch.admin.foitt.openid4vc.domain.model.jwk.Jwk
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AppAttestationRepositoryError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AttestationChallengeResponse
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestationRequest
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestationResponse
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.Confirmation
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.IntegrityToken
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.KeyAttestationRequest
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.KeyAttestationResponse
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.toAppAttestationRepositoryError
import ch.admin.foitt.wallet.platform.appAttestation.domain.repository.AppAttestationRepository
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject
import javax.inject.Named

class AppAttestationRepositoryImpl @Inject constructor(
    @param:Named(NAMED_DEFAULT_HTTP_CLIENT) private val httpClient: HttpClient,
    private val environmentSetupRepo: EnvironmentSetupRepository,
) : AppAttestationRepository {

    override suspend fun fetchChallenge() = runSuspendCatching<AttestationChallengeResponse> {
        httpClient.get(environmentSetupRepo.attestationsServiceUrl + "/api/attestations/challenge") {
            contentType(ContentType.Application.Json)
        }.body()
    }.mapError { throwable ->
        throwable.toAppAttestationRepositoryError("Fetch challenge failed")
    }

    override suspend fun fetchClientAttestation(
        integrityToken: IntegrityToken,
        publicKey: Jwk,
    ): Result<ClientAttestationResponse, AppAttestationRepositoryError> = runSuspendCatching<ClientAttestationResponse> {
        val body = ClientAttestationRequest(
            integrityToken = integrityToken.value,
            cnf = Confirmation(publicKey),
        )
        httpClient.post(environmentSetupRepo.attestationsServiceUrl + "/api/attestations/android/client-attestations") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()
    }.mapError { throwable ->
        throwable.toAppAttestationRepositoryError("Fetch challenge failed")
    }

    override suspend fun fetchKeyAttestation(publicKey: Jwk) = runSuspendCatching<KeyAttestationResponse> {
        val body = KeyAttestationRequest(
            cnf = Confirmation(publicKey),
        )

        httpClient.post(environmentSetupRepo.attestationsServiceUrl + "/api/attestations/android/key-attestations") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()
    }.mapError { throwable ->
        throwable.toAppAttestationRepositoryError("Fetch key attestation failed")
    }
}
