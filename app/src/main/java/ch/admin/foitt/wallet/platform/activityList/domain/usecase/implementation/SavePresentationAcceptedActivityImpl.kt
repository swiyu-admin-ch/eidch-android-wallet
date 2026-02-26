package ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityType
import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityRepository
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.SavePresentationAcceptedActivity
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import javax.inject.Inject

class SavePresentationAcceptedActivityImpl @Inject constructor(
    private val activityRepository: ActivityRepository,
) : SavePresentationAcceptedActivity {

    override suspend fun invoke(
        credentialId: Long,
        actorDisplayData: ActorDisplayData,
        verifierFallbackName: String,
        claimIds: List<Long>,
        nonComplianceData: String?,
    ) {
        activityRepository.saveActivity(
            activityType = ActivityType.PRESENTATION_ACCEPTED,
            credentialId = credentialId,
            actorDisplayData = actorDisplayData,
            actorFallbackName = verifierFallbackName,
            claimIds = claimIds,
            nonComplianceData = nonComplianceData,
        )
    }
}
