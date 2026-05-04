package ch.admin.foitt.wallet.feature.otp.domain.usecase

import ch.admin.foitt.wallet.feature.otp.domain.model.OtpVerify
import ch.admin.foitt.wallet.feature.otp.domain.model.RequestOtpError
import com.github.michaelbull.result.Result

interface VerifyOtp {
    suspend operator fun invoke(otpVerify: OtpVerify): Result<Unit, RequestOtpError>
}
