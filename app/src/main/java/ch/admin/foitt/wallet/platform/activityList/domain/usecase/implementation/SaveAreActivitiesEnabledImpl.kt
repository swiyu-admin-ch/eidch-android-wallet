package ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityStateRepository
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.SaveAreActivitiesEnabled
import javax.inject.Inject

class SaveAreActivitiesEnabledImpl @Inject constructor(
    private val activityStateRepository: ActivityStateRepository,
) : SaveAreActivitiesEnabled {
    override suspend fun invoke(enabled: Boolean) {
        activityStateRepository.saveAreActivitiesEnabled(enabled = enabled)
    }
}
