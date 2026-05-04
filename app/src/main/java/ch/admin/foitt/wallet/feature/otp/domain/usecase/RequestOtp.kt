package ch.admin.foitt.wallet.feature.otp.domain.usecase

import ch.admin.foitt.wallet.feature.otp.domain.model.OtpRequest
import ch.admin.foitt.wallet.feature.otp.domain.model.RequestOtpError
import com.github.michaelbull.result.Result

interface RequestOtp {
    suspend operator fun invoke(otpRequest: OtpRequest): Result<Unit, RequestOtpError>
}
