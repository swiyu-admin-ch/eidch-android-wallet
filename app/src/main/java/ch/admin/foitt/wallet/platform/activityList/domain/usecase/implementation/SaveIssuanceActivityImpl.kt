package ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityType
import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityRepository
import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityStateRepository
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.SaveIssuanceActivity
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import javax.inject.Inject

class SaveIssuanceActivityImpl @Inject constructor(
    private val activityStateRepository: ActivityStateRepository,
    private val activityRepository: ActivityRepository,
) : SaveIssuanceActivity {
    override suspend fun invoke(
        credentialId: Long,
        actorDisplayData: ActorDisplayData,
        issuerFallbackName: String,
    ) {
        if (activityStateRepository.areActivitiesEnabled()) {
            activityRepository.saveActivity(
                activityType = ActivityType.ISSUANCE,
                credentialId = credentialId,
                actorDisplayData = actorDisplayData,
                actorFallbackName = issuerFallbackName,
                nonComplianceData = null,
            )
        }
    }
}
