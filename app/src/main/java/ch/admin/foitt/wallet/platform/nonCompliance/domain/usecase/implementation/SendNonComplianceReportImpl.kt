package ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceIssuerReportReason
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceReportReason
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceVerifierReportReason
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.SendNonComplianceReportError
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.SendIssuerNonComplianceReport
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.SendNonComplianceReport
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.SendVerifierNonComplianceReport
import com.github.michaelbull.result.Result
import javax.inject.Inject

class SendNonComplianceReportImpl @Inject constructor(
    private val sendIssuerReport: SendIssuerNonComplianceReport,
    private val sendVerifierReport: SendVerifierNonComplianceReport,
) : SendNonComplianceReport {
    override suspend fun invoke(
        activityId: Long?,
        reportReason: NonComplianceReportReason,
        description: String?,
        context: String?,
        email: String?
    ): Result<Unit, SendNonComplianceReportError> = when (reportReason) {
        is NonComplianceIssuerReportReason -> sendIssuerReport(activityId, reportReason, description, context, email)
        is NonComplianceVerifierReportReason -> sendVerifierReport(activityId, reportReason, description, context, email)
    }
}
