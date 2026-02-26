package ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceTextInputConstraints
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceTextLengthValidationState
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceValidationState
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.ValidateTextLength
import javax.inject.Inject

class ValidateTextLengthImpl @Inject constructor(
    private val nonComplianceTextInputConstraints: NonComplianceTextInputConstraints,
) : ValidateTextLength {
    override fun invoke(text: String): NonComplianceTextLengthValidationState {
        return when {
            text.length < nonComplianceTextInputConstraints.minLength -> NonComplianceValidationState.TooShort
            text.length > nonComplianceTextInputConstraints.maxLength -> NonComplianceValidationState.TooLong
            else -> NonComplianceValidationState.Valid
        }
    }
}
