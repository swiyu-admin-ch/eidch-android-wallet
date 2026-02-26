package ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase

import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceEmailValidationState

interface ValidateEmail {
    operator fun invoke(email: String): NonComplianceEmailValidationState
}
