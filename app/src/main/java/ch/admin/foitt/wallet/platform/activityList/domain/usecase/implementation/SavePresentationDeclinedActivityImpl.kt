package ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityType
import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityRepository
import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityStateRepository
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.SavePresentationDeclinedActivity
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import javax.inject.Inject

class SavePresentationDeclinedActivityImpl @Inject constructor(
    private val activityStateRepository: ActivityStateRepository,
    private val activityRepository: ActivityRepository,
) : SavePresentationDeclinedActivity {

    override suspend fun invoke(
        credentialId: Long,
        actorDisplayData: ActorDisplayData,
        verifierFallbackName: String,
        claimIds: List<Long>,
        nonComplianceData: String?,
    ) {
        if (activityStateRepository.areActivitiesEnabled()) {
            activityRepository.saveActivity(
                activityType = ActivityType.PRESENTATION_DECLINED,
                credentialId = credentialId,
                actorDisplayData = actorDisplayData,
                actorFallbackName = verifierFallbackName,
                claimIds = claimIds,
                nonComplianceData = nonComplianceData,
            )
        }
    }
}
