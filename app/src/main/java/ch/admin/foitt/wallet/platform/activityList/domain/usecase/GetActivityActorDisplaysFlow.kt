package ch.admin.foitt.wallet.platform.activityList.domain.usecase

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityActorDisplayData
import ch.admin.foitt.wallet.platform.activityList.domain.model.GetActivityActorDisplaysFlowError
import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow

interface GetActivityActorDisplaysFlow {
    operator fun invoke(activityId: Long): Flow<Result<ActivityActorDisplayData, GetActivityActorDisplaysFlowError>>
}
