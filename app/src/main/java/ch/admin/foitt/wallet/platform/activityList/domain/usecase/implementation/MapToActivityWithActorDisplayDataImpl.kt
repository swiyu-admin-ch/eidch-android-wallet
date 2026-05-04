package ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityWithActorDisplayData
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.MapToActivityWithActorDisplayData
import ch.admin.foitt.wallet.platform.database.domain.model.ActivityWithActorDisplays
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetCurrentAppLocale
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import ch.admin.foitt.wallet.platform.utils.asDayMonthYearHoursMinutesWithPipe
import ch.admin.foitt.wallet.platform.utils.epochSecondsToZonedDateTime
import javax.inject.Inject

class MapToActivityWithActorDisplayDataImpl @Inject constructor(
    private val getCurrentAppLocale: GetCurrentAppLocale,
    private val getLocalizedDisplay: GetLocalizedDisplay,
) : MapToActivityWithActorDisplayData {
    override fun invoke(activity: ActivityWithActorDisplays) = getActivityDisplayData(activity)

    private fun getActivityDisplayData(
        activityWithActorDisplays: ActivityWithActorDisplays,
    ): ActivityWithActorDisplayData {
        val appLocale = getCurrentAppLocale()
        val zonedDateTime = activityWithActorDisplays.activity.createdAt.epochSecondsToZonedDateTime()
        val formattedDate = zonedDateTime.asDayMonthYearHoursMinutesWithPipe(locale = appLocale)

        val actorDisplays = activityWithActorDisplays.actorDisplays.map { it.actorDisplay }
        val localizedActorName = getLocalizedDisplay(actorDisplays)?.name ?: ""

        return ActivityWithActorDisplayData(
            activityId = activityWithActorDisplays.activity.id,
            activityType = activityWithActorDisplays.activity.type,
            date = formattedDate,
            localizedActorName = localizedActorName,
        )
    }
}
