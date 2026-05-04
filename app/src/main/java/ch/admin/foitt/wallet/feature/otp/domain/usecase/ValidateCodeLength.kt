package ch.admin.foitt.wallet.feature.otp.domain.usecase

import ch.admin.foitt.wallet.feature.otp.domain.model.OtpCodeLengthValidationState

interface ValidateCodeLength {
    operator fun invoke(text: String): OtpCodeLengthValidationState
}
