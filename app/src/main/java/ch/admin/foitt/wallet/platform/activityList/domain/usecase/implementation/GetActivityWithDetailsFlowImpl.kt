package ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityWithDetails
import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityWithDetailsRepositoryError
import ch.admin.foitt.wallet.platform.activityList.domain.model.GetActivityWithDetailsFlowError
import ch.admin.foitt.wallet.platform.activityList.domain.model.toGetActivityWithDetailsFlowError
import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityWithDetailsRepository
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.GetActivityWithDetailsFlow
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.MapToActivityDisplayData
import ch.admin.foitt.wallet.platform.utils.andThen
import ch.admin.foitt.wallet.platform.utils.mapError
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActivityWithDetailsFlowImpl @Inject constructor(
    private val activityWithDetailsRepository: ActivityWithDetailsRepository,
    private val mapToActivityDisplayData: MapToActivityDisplayData,
) : GetActivityWithDetailsFlow {
    override fun invoke(
        activityId: Long,
    ): Flow<Result<ActivityWithDetails, GetActivityWithDetailsFlowError>> = activityWithDetailsRepository.getByIdFlow(activityId)
        .mapError(ActivityWithDetailsRepositoryError::toGetActivityWithDetailsFlowError)
        .andThen { activityWithDetails ->
            coroutineBinding {
                val activityDisplayData = mapToActivityDisplayData(activityWithDetails)

                ActivityWithDetails(
                    activity = activityDisplayData,
                )
            }
        }
}
