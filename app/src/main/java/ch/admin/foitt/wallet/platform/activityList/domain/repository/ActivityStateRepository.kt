package ch.admin.foitt.wallet.platform.activityList.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface ActivityStateRepository {
    suspend fun saveAreActivitiesEnabled(enabled: Boolean)
    fun areActivitiesEnabled(): Boolean
    fun areActivitiesEnabledFlow(): StateFlow<Boolean>
}
