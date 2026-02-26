package ch.admin.foitt.wallet.platform.eIdApplicationProcess.data.repository

import android.content.Context
import ch.admin.foitt.openid4vc.di.ExternalOpenId4VcModule.Companion.NAMED_DEFAULT_HTTP_CLIENT
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestationPoP
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.KeyAttestation
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.ApplyRequest
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.AttestationsValidationRequest
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.AutoVerificationResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.CaseResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdStartAutoVerificationType
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.GuardianVerificationResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.PairWalletResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.SIdChallengeResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.SIdRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.StateResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.WalletPairingStateResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.toSIdRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.toValidateAttestationsError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.SIdRepository
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.utils.hasNFCHardware
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject
import javax.inject.Named

class SIdRepositoryImpl @Inject constructor(
    @param:Named(NAMED_DEFAULT_HTTP_CLIENT) private val httpClient: HttpClient,
    private val environmentSetupRepo: EnvironmentSetupRepository,
    @param:ApplicationContext private val appContext: Context,
) : SIdRepository {

    override suspend fun requestSIdCase(
        clientAttestation: ClientAttestation,
        clientAttestationPoP: ClientAttestationPoP,
        applyRequest: ApplyRequest,
    ): Result<CaseResponse, SIdRepositoryError> =
        runSuspendCatching<CaseResponse> {
            httpClient.post(environmentSetupRepo.sidBackendUrl + REST_API + "eid/apply") {
                header(ClientAttestation.REQUEST_HEADER, clientAttestation.attestation.rawJwt)
                header(ClientAttestationPoP.REQUEST_HEADER, clientAttestationPoP.value)
                contentType(ContentType.Application.Json)
                setBody(applyRequest)
            }.body()
        }.mapError { throwable ->
            throwable.toSIdRepositoryError("fetchSIdCase error")
        }

    override suspend fun fetchSIdState(
        caseId: String,
        clientAttestation: ClientAttestation,
    ): Result<StateResponse, SIdRepositoryError> =
        runSuspendCatching<StateResponse> {
            httpClient.get(environmentSetupRepo.sidBackendUrl + REST_API + "eid/$caseId/state") {
                header(ClientAttestation.REQUEST_HEADER, clientAttestation.attestation.rawJwt)
                contentType(ContentType.Application.Json)
            }.body()
        }.mapError { throwable ->
            throwable.toSIdRepositoryError("fetchSIdState error")
        }

    override suspend fun fetchSIdGuardianVerification(
        caseId: String,
        clientAttestation: ClientAttestation,
    ): Result<GuardianVerificationResponse, SIdRepositoryError> =
        runSuspendCatching<GuardianVerificationResponse> {
            httpClient.get(environmentSetupRepo.sidBackendUrl + REST_API + "eid/$caseId/legal-representant-verification") {
                header(ClientAttestation.REQUEST_HEADER, clientAttestation.attestation.rawJwt)
                contentType(ContentType.Application.Json)
            }.body()
        }.mapError { throwable ->
            throwable.toSIdRepositoryError("fetchSIdGuardianVerification error")
        }

    override suspend fun validateAttestations(
        clientAttestation: ClientAttestation,
        keyAttestation: KeyAttestation,
    ) = runSuspendCatching<Unit> {
        val attestationsValidationRequest = AttestationsValidationRequest(
            clientAttestation = clientAttestation.attestation.rawJwt,
            keyAttestation = keyAttestation.attestation.rawJwt
        )
        httpClient.post(environmentSetupRepo.sidBackendUrl + REST_API + "attestations/validate") {
            contentType(ContentType.Application.Json)
            setBody(attestationsValidationRequest)
        }.body()
    }.mapError { throwable ->
        throwable.toValidateAttestationsError("validateAttestations error")
    }

    override suspend fun fetchChallenge() = runSuspendCatching<SIdChallengeResponse> {
        httpClient.get(environmentSetupRepo.sidBackendUrl + REST_API + "eid/challenge") {
            contentType(ContentType.Application.Json)
        }.body()
    }.mapError { throwable ->
        throwable.toSIdRepositoryError("fetchChallenge error")
    }

    override suspend fun startOnlineSession(
        caseId: String,
        clientAttestation: ClientAttestation,
        clientAttestationPoP: ClientAttestationPoP,
    ) = runSuspendCatching<Unit> {
        httpClient.put(environmentSetupRepo.sidBackendUrl + REST_API + "eid/$caseId/start-online-session") {
            header(ClientAttestation.REQUEST_HEADER, clientAttestation.attestation.rawJwt)
            header(ClientAttestationPoP.REQUEST_HEADER, clientAttestationPoP.value)
        }.body()
    }.mapError { throwable ->
        throwable.toSIdRepositoryError("startOnlineSession error")
    }

    override suspend fun pairWallet(
        caseId: String,
        clientAttestation: ClientAttestation,
        clientAttestationPoP: ClientAttestationPoP,
    ) = runSuspendCatching<PairWalletResponse> {
        httpClient.put(environmentSetupRepo.sidBackendUrl + REST_API + "eid/$caseId/pair-wallet") {
            header(ClientAttestation.REQUEST_HEADER, clientAttestation.attestation.rawJwt)
            header(ClientAttestationPoP.REQUEST_HEADER, clientAttestationPoP.value)
        }.body()
    }.mapError { throwable ->
        throwable.toSIdRepositoryError("pairWallet error")
    }

    override suspend fun startAutoVerification(
        caseId: String,
        autoVerificationType: EIdStartAutoVerificationType,
        clientAttestation: ClientAttestation,
        clientAttestationPoP: ClientAttestationPoP
    ): Result<AutoVerificationResponse, SIdRepositoryError> =
        runSuspendCatching<AutoVerificationResponse> {
            val hasNFC = appContext.hasNFCHardware()
            httpClient.put(
                environmentSetupRepo.sidBackendUrl + REST_API +
                    "eid/$caseId/start-auto-verification/$autoVerificationType"
            ) {
                header(ClientAttestation.REQUEST_HEADER, clientAttestation.attestation.rawJwt)
                header(ClientAttestationPoP.REQUEST_HEADER, clientAttestationPoP.value)
                parameter("nfcAvailable", hasNFC)
            }.body()
        }.mapError { throwable ->
            throwable.toSIdRepositoryError("startAutoVerification error")
        }

    override suspend fun getWalletPairingState(
        caseId: String,
        walletPairingId: String,
        clientAttestation: ClientAttestation,
    ): Result<WalletPairingStateResponse, SIdRepositoryError> =
        runSuspendCatching<WalletPairingStateResponse> {
            httpClient.get(
                environmentSetupRepo.sidBackendUrl + REST_API +
                    "eid/$caseId/pair-wallet/$walletPairingId/state"
            ) {
                header(ClientAttestation.REQUEST_HEADER, clientAttestation.attestation.rawJwt)
                contentType(ContentType.Application.Json)
            }.body()
        }.mapError { throwable ->
            throwable.toSIdRepositoryError("getWalletPairingState error")
        }

    companion object {
        private const val REST_API = "api/rest/"
    }
}
