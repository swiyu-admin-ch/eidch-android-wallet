package ch.admin.foitt.wallet.platform.activityList.presentation.model

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityType
import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityWithActorDisplayData

data class ActivityUiState(
    val id: Long,
    val activityType: ActivityType,
    val date: String,
    val localizedActorName: String,
)

fun ActivityWithActorDisplayData.toActivityUiState() = ActivityUiState(
    id = activityId,
    activityType = activityType,
    date = date,
    localizedActorName = localizedActorName,
)
