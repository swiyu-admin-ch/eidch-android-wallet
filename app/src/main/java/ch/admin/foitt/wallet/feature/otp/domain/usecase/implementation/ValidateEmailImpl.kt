package ch.admin.foitt.wallet.feature.otp.domain.usecase.implementation

import androidx.core.util.PatternsCompat
import ch.admin.foitt.wallet.feature.otp.domain.model.OtpEmailValidationState
import ch.admin.foitt.wallet.feature.otp.domain.model.OtpValidationState
import ch.admin.foitt.wallet.feature.otp.domain.usecase.ValidateEmail
import javax.inject.Inject

class ValidateEmailImpl @Inject constructor() : ValidateEmail {
    override fun invoke(email: String): OtpEmailValidationState =
        if (PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()) {
            OtpValidationState.Valid
        } else {
            OtpValidationState.Invalid
        }
}
