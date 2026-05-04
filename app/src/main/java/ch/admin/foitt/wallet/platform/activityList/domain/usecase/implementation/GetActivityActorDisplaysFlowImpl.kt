package ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityActorDisplayData
import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityActorDisplayWithImageRepositoryError
import ch.admin.foitt.wallet.platform.activityList.domain.model.GetActivityActorDisplaysFlowError
import ch.admin.foitt.wallet.platform.activityList.domain.model.toGetActivityActorDisplaysFlowError
import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityActorDisplayWithImageRepository
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.GetActivityActorDisplaysFlow
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.MapToActivityActorDisplayData
import ch.admin.foitt.wallet.platform.utils.andThen
import ch.admin.foitt.wallet.platform.utils.mapError
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActivityActorDisplaysFlowImpl @Inject constructor(
    private val activityActorDisplayWithImageRepository: ActivityActorDisplayWithImageRepository,
    private val mapToActivityActorDisplayData: MapToActivityActorDisplayData,
) : GetActivityActorDisplaysFlow {
    override fun invoke(activityId: Long): Flow<Result<ActivityActorDisplayData, GetActivityActorDisplaysFlowError>> =
        activityActorDisplayWithImageRepository.getActorDisplaysWithImageByActivityIdFlow(activityId)
            .mapError(ActivityActorDisplayWithImageRepositoryError::toGetActivityActorDisplaysFlowError)
            .andThen { activityActorDisplayWithImages ->
                coroutineBinding {
                    val activityActorDisplayData = mapToActivityActorDisplayData(
                        activityId = activityId,
                        actorDisplaysWithImages = activityActorDisplayWithImages,
                    )

                    activityActorDisplayData
                }
            }
}
