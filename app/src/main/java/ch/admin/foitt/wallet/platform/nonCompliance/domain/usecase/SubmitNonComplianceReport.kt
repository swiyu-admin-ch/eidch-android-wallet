package ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase

import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceRequest
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.SendNonComplianceReportError
import com.github.michaelbull.result.Result

interface SubmitNonComplianceReport {
    suspend operator fun invoke(
        request: NonComplianceRequest
    ): Result<Unit, SendNonComplianceReportError>
}
