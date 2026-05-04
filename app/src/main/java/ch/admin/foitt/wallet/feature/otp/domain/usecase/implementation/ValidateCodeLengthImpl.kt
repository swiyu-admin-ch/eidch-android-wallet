package ch.admin.foitt.wallet.feature.otp.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.otp.domain.model.OtpCodeLengthValidationState
import ch.admin.foitt.wallet.feature.otp.domain.model.OtpValidationState
import ch.admin.foitt.wallet.feature.otp.domain.usecase.ValidateCodeLength
import javax.inject.Inject

class ValidateCodeLengthImpl @Inject constructor() : ValidateCodeLength {
    override fun invoke(text: String): OtpCodeLengthValidationState {
        return when {
            text.length < MIN_LENGTH -> OtpValidationState.TooShort
            else -> OtpValidationState.Valid
        }
    }

    companion object {
        private const val MIN_LENGTH = 6
    }
}
