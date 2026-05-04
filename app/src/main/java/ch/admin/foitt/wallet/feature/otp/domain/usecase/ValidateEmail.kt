package ch.admin.foitt.wallet.feature.otp.domain.usecase

import ch.admin.foitt.wallet.feature.otp.domain.model.OtpEmailValidationState

interface ValidateEmail {
    operator fun invoke(email: String): OtpEmailValidationState
}
