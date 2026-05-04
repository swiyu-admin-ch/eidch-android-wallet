package ch.admin.foitt.wallet.feature.otp.data.repository

import ch.admin.foitt.openid4vc.di.ExternalOpenId4VcModule.Companion.NAMED_DEFAULT_HTTP_CLIENT
import ch.admin.foitt.wallet.feature.otp.domain.model.OtpRequest
import ch.admin.foitt.wallet.feature.otp.domain.model.OtpVerify
import ch.admin.foitt.wallet.feature.otp.domain.model.RequestOtpError
import ch.admin.foitt.wallet.feature.otp.domain.model.toRequestOtpError
import ch.admin.foitt.wallet.feature.otp.domain.repository.OtpRepository
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestationPoP
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetCurrentAppLocale
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import javax.inject.Inject
import javax.inject.Named

class OtpRepositoryImpl @Inject constructor(
    @param:Named(NAMED_DEFAULT_HTTP_CLIENT) private val httpClient: HttpClient,
    private val environmentSetupRepo: EnvironmentSetupRepository,
    private val getCurrentAppLocale: GetCurrentAppLocale,
) : OtpRepository {

    override suspend fun requestOTP(
        clientAttestation: ClientAttestation,
        clientAttestationPoP: ClientAttestationPoP,
        otpRequest: OtpRequest,
    ): Result<Unit, RequestOtpError> = runSuspendCatching<Unit> {
        httpClient.post(environmentSetupRepo.attestationsServiceUrl + "/otp/request") {
            header(ClientAttestation.REQUEST_HEADER, clientAttestation.attestation.rawJwt)
            header(ClientAttestationPoP.REQUEST_HEADER, clientAttestationPoP.value)
            header(HttpHeaders.AcceptLanguage, getCurrentAppLocale())
            contentType(ContentType.Application.Json)
            setBody(otpRequest)
        }.body()
    }.mapError { throwable ->
        throwable.toRequestOtpError("request OTP failed")
    }

    override suspend fun verifyOTP(
        clientAttestation: ClientAttestation,
        clientAttestationPoP: ClientAttestationPoP,
        otpVerify: OtpVerify,
    ): Result<Unit, RequestOtpError> = runSuspendCatching<Unit> {
        httpClient.post(environmentSetupRepo.attestationsServiceUrl + "/otp/verify") {
            header(ClientAttestation.REQUEST_HEADER, clientAttestation.attestation.rawJwt)
            header(ClientAttestationPoP.REQUEST_HEADER, clientAttestationPoP.value)
            contentType(ContentType.Application.Json)
            setBody(otpVerify)
        }.body()
    }.mapError { throwable ->
        throwable.toRequestOtpError("verify OTP failed")
    }
}
