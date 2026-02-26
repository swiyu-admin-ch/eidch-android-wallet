package ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityActorDisplayData
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.MapToActivityActorDisplayData
import ch.admin.foitt.wallet.platform.database.domain.model.ActivityActorDisplayWithImage
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import javax.inject.Inject

class MapToActivityActorDisplayDataImpl @Inject constructor(
    private val getLocalizedDisplay: GetLocalizedDisplay,
) : MapToActivityActorDisplayData {
    override suspend fun invoke(
        activityId: Long,
        actorDisplaysWithImages: List<ActivityActorDisplayWithImage>,
    ): ActivityActorDisplayData {
        val actorDisplays = actorDisplaysWithImages.map { it.actorDisplay }
        val localizedActorDisplay = getLocalizedDisplay(actorDisplays)
        val imageData = actorDisplaysWithImages.find { it.actorDisplay == localizedActorDisplay }?.image?.image

        return ActivityActorDisplayData(
            id = activityId,
            localizedActorName = localizedActorDisplay?.name ?: "",
            actorImageData = imageData
        )
    }
}
