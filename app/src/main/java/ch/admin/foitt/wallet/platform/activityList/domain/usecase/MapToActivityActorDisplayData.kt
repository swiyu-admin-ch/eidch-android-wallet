package ch.admin.foitt.wallet.platform.activityList.domain.usecase

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityActorDisplayData
import ch.admin.foitt.wallet.platform.database.domain.model.ActivityActorDisplayWithImage

interface MapToActivityActorDisplayData {
    suspend operator fun invoke(
        activityId: Long,
        actorDisplaysWithImages: List<ActivityActorDisplayWithImage>,
    ): ActivityActorDisplayData
}
