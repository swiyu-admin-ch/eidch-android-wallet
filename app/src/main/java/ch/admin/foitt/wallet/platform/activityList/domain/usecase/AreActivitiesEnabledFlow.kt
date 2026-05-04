package ch.admin.foitt.wallet.platform.activityList.domain.usecase

import kotlinx.coroutines.flow.StateFlow

interface AreActivitiesEnabledFlow {
    operator fun invoke(): StateFlow<Boolean>
}
