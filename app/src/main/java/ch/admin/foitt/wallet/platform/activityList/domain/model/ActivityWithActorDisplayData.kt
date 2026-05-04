package ch.admin.foitt.wallet.platform.activityList.domain.model

data class ActivityWithActorDisplayData(
    val activityId: Long,
    val activityType: ActivityType,
    val localizedActorName: String,
    val date: String,
)
