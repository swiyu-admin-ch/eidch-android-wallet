package ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityDisplayData
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.MapToActivityActorDisplayData
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.MapToActivityDisplayData
import ch.admin.foitt.wallet.platform.database.domain.model.ActivityWithDisplays
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetCurrentAppLocale
import ch.admin.foitt.wallet.platform.utils.asDayMonthYearHoursMinutesWithPipe
import ch.admin.foitt.wallet.platform.utils.epochSecondsToZonedDateTime
import javax.inject.Inject

class MapToActivityDisplayDataImpl @Inject constructor(
    private val getCurrentAppLocale: GetCurrentAppLocale,
    private val mapToActivityActorDisplayData: MapToActivityActorDisplayData,
) : MapToActivityDisplayData {
    override suspend fun invoke(activity: ActivityWithDisplays) = getActivityDisplayData(activity)

    private suspend fun getActivityDisplayData(
        activityWithDisplays: ActivityWithDisplays,
    ): ActivityDisplayData {
        val appLocale = getCurrentAppLocale()
        val zonedDateTime = activityWithDisplays.activity.createdAt.epochSecondsToZonedDateTime()
        val formattedDate = zonedDateTime.asDayMonthYearHoursMinutesWithPipe(locale = appLocale)

        val actorDisplayData = mapToActivityActorDisplayData(
            activityId = activityWithDisplays.activity.id,
            actorDisplaysWithImages = activityWithDisplays.actorDisplays,
        )

        return ActivityDisplayData(
            id = activityWithDisplays.activity.id,
            activityType = activityWithDisplays.activity.type,
            date = formattedDate,
            nonComplianceData = activityWithDisplays.activity.nonComplianceData,
            localizedActorName = actorDisplayData.localizedActorName,
            actorImageData = actorDisplayData.actorImageData
        )
    }
}
