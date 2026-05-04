package ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityDetailDisplayData
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.MapToActivityDetailDisplayData
import ch.admin.foitt.wallet.platform.database.domain.model.ActivityWithDetails
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetCurrentAppLocale
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import ch.admin.foitt.wallet.platform.utils.asDayMonthYearHoursMinutesWithPipe
import ch.admin.foitt.wallet.platform.utils.epochSecondsToZonedDateTime
import javax.inject.Inject

class MapToActivityDetailDisplayDataImpl @Inject constructor(
    private val getCurrentAppLocale: GetCurrentAppLocale,
    private val getLocalizedDisplay: GetLocalizedDisplay,
) : MapToActivityDetailDisplayData {
    override fun invoke(activity: ActivityWithDetails) = getActivityDisplayData(activity)

    private fun getActivityDisplayData(
        activityWithDetails: ActivityWithDetails,
    ): ActivityDetailDisplayData {
        val appLocale = getCurrentAppLocale()
        val zonedDateTime = activityWithDetails.activity.createdAt.epochSecondsToZonedDateTime()
        val formattedDate = zonedDateTime.asDayMonthYearHoursMinutesWithPipe(locale = appLocale)

        val actorDisplays = activityWithDetails.actorDisplays.map { it.actorDisplay }
        val localizedActorDisplay = getLocalizedDisplay(actorDisplays)
        val localizedActorImageData = activityWithDetails.actorDisplays.find {
            it.actorDisplay == localizedActorDisplay
        }?.image?.image

        val localizedNonComplianceReason = getLocalizedDisplay(activityWithDetails.nonComplianceReasonDisplays)?.reason

        return ActivityDetailDisplayData(
            activityId = activityWithDetails.activity.id,
            activityType = activityWithDetails.activity.type,
            date = formattedDate,
            localizedActorName = localizedActorDisplay?.name ?: "",
            actorTrustStatus = activityWithDetails.activity.actorTrust,
            vcSchemaTrustStatus = activityWithDetails.activity.vcSchemaTrust,
            actorComplianceState = activityWithDetails.activity.actorCompliance,
            localizedNonComplianceReason = localizedNonComplianceReason,
            actorImageData = localizedActorImageData,
        )
    }
}
