package ch.admin.foitt.wallet.platform.activityList.domain.usecase

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityDetailDisplayData
import ch.admin.foitt.wallet.platform.database.domain.model.ActivityWithDetails

interface MapToActivityDetailDisplayData {
    operator fun invoke(
        activity: ActivityWithDetails,
    ): ActivityDetailDisplayData
}
