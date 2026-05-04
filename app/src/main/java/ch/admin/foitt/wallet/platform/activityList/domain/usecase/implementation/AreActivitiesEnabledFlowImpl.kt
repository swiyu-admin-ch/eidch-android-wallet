package ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityStateRepository
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.AreActivitiesEnabledFlow
import javax.inject.Inject

class AreActivitiesEnabledFlowImpl @Inject constructor(
    private val activityStateRepository: ActivityStateRepository,
) : AreActivitiesEnabledFlow {
    override fun invoke() = activityStateRepository.areActivitiesEnabledFlow()
}
