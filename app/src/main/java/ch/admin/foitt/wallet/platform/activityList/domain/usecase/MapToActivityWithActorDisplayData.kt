package ch.admin.foitt.wallet.platform.activityList.domain.usecase

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityWithActorDisplayData
import ch.admin.foitt.wallet.platform.database.domain.model.ActivityWithActorDisplays

interface MapToActivityWithActorDisplayData {
    operator fun invoke(
        activity: ActivityWithActorDisplays
    ): ActivityWithActorDisplayData
}
