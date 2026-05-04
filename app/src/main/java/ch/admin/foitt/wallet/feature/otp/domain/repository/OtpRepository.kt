package ch.admin.foitt.wallet.feature.otp.domain.repository

import ch.admin.foitt.wallet.feature.otp.domain.model.OtpRequest
import ch.admin.foitt.wallet.feature.otp.domain.model.OtpVerify
import ch.admin.foitt.wallet.feature.otp.domain.model.RequestOtpError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestationPoP
import com.github.michaelbull.result.Result

interface OtpRepository {
    suspend fun requestOTP(
        clientAttestation: ClientAttestation,
        clientAttestationPoP: ClientAttestationPoP,
        otpRequest: OtpRequest
    ): Result<Unit, RequestOtpError>
    suspend fun verifyOTP(
        clientAttestation: ClientAttestation,
        clientAttestationPoP: ClientAttestationPoP,
        otpVerify: OtpVerify
    ): Result<Unit, RequestOtpError>
}
