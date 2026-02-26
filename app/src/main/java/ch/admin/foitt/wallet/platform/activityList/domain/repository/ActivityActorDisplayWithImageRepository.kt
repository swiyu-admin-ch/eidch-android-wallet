package ch.admin.foitt.wallet.platform.activityList.domain.repository

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityActorDisplayWithImageRepositoryError
import ch.admin.foitt.wallet.platform.database.domain.model.ActivityActorDisplayWithImage
import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow

interface ActivityActorDisplayWithImageRepository {
    fun getActorDisplaysWithImageByActivityIdFlow(
        activityId: Long
    ): Flow<Result<List<ActivityActorDisplayWithImage>, ActivityActorDisplayWithImageRepositoryError>>
}
