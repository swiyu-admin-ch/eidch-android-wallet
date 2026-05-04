package ch.admin.foitt.wallet.platform.activityList.domain.usecase

interface SaveAreActivitiesEnabled {
    suspend operator fun invoke(enabled: Boolean)
}
