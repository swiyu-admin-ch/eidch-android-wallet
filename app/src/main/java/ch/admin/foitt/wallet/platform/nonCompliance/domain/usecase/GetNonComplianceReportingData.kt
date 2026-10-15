package ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityType
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.GetNonComplianceReportingDataError
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceReportingData
import com.github.michaelbull.result.Result

interface GetNonComplianceReportingData {
    suspend operator fun invoke(
        credentialId: Long,
        actorDisplayData: ActorDisplayData,
        activityType: ActivityType,
        activityId: Long? = null,
    ): Result<NonComplianceReportingData, GetNonComplianceReportingDataError>
}
