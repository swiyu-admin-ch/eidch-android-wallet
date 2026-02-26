package ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase

import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceReportReason
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.SendNonComplianceReportError
import com.github.michaelbull.result.Result

interface SendNonComplianceReport {
    suspend operator fun invoke(
        activityId: Long,
        reportReason: NonComplianceReportReason,
        description: String,
        email: String?,
    ): Result<Unit, SendNonComplianceReportError>
}
