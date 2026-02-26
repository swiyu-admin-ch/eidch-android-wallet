package ch.admin.foitt.wallet.platform.activityList.domain.usecase

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityWithDetails
import ch.admin.foitt.wallet.platform.activityList.domain.model.GetActivityWithDetailsFlowError
import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow

interface GetActivityWithDetailsFlow {
    operator fun invoke(
        activityId: Long,
    ): Flow<Result<ActivityWithDetails, GetActivityWithDetailsFlowError>>
}
