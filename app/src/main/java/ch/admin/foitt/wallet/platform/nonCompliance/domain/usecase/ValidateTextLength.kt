package ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase

import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceTextLengthValidationState

interface ValidateTextLength {
    operator fun invoke(text: String): NonComplianceTextLengthValidationState
}
