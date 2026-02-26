package ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.implementation

import androidx.core.util.PatternsCompat
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceEmailValidationState
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceValidationState
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.ValidateEmail
import javax.inject.Inject

class ValidateEmailImpl @Inject constructor() : ValidateEmail {
    override fun invoke(email: String): NonComplianceEmailValidationState =
        if (PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()) {
            NonComplianceValidationState.Valid
        } else {
            NonComplianceValidationState.Invalid
        }
}
