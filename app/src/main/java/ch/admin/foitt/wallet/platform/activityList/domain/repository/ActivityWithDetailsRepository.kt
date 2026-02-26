package ch.admin.foitt.wallet.platform.activityList.domain.repository

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityWithDetailsRepositoryError
import ch.admin.foitt.wallet.platform.database.domain.model.ActivityWithDetails
import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow

interface ActivityWithDetailsRepository {
    fun getNullableByIdFlow(activityId: Long): Flow<Result<ActivityWithDetails?, ActivityWithDetailsRepositoryError>>
    fun getByIdFlow(activityId: Long): Flow<Result<ActivityWithDetails, ActivityWithDetailsRepositoryError>>
}
