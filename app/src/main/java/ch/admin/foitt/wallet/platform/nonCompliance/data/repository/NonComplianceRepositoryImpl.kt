
package ch.admin.foitt.wallet.platform.nonCompliance.data.repository

import ch.admin.foitt.openid4vc.di.ExternalOpenId4VcModule.Companion.NAMED_DEFAULT_HTTP_CLIENT
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestationPoP
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceChallengeResponse
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceRepositoryError
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceRequest
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.toNonComplianceRepositoryError
import ch.admin.foitt.wallet.platform.nonCompliance.domain.repository.NonComplianceRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import java.net.URL
import javax.inject.Inject
import javax.inject.Named

class NonComplianceRepositoryImpl @Inject constructor(
    @param:Named(NAMED_DEFAULT_HTTP_CLIENT) private val httpClient: HttpClient,
    private val environmentSetupRepository: EnvironmentSetupRepository,
) : NonComplianceRepository {
    override suspend fun fetchChallenge() = runSuspendCatching<NonComplianceChallengeResponse> {
        val baseUrl = environmentSetupRepository.nonComplianceBaseUrl
        httpClient.get("$baseUrl/mobile-api/v1/challenge") {
            contentType(ContentType.Application.Json)
        }.body()
    }.mapError { throwable ->
        throwable.toNonComplianceRepositoryError("fetch non-compliance challenge error")
    }

    override suspend fun sendReport(
        clientAttestation: ClientAttestation,
        clientAttestationPoP: ClientAttestationPoP,
        nonComplianceRequest: NonComplianceRequest,
    ): Result<Unit, NonComplianceRepositoryError> = runSuspendCatching<Unit> {
        val baseUrl = environmentSetupRepository.nonComplianceBaseUrl
        val url = URL("$baseUrl/mobile-api/v1/cases/non-compliant-actors")
        httpClient.post(url) {
            header(ClientAttestation.REQUEST_HEADER, clientAttestation.attestation.rawJwt)
            header(ClientAttestationPoP.REQUEST_HEADER, clientAttestationPoP.value)
            contentType(ContentType.Application.Json)
            setBody(nonComplianceRequest)
        }.body()
    }.mapError { throwable ->
        throwable.toNonComplianceRepositoryError("error when sending non compliance report")
    }
}
