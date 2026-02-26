package ch.admin.foitt.wallet.platform.activityList.data.repository

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityActorDisplayWithImageRepositoryError
import ch.admin.foitt.wallet.platform.activityList.domain.model.toActivityActorDisplayWithImageRepositoryError
import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityActorDisplayWithImageRepository
import ch.admin.foitt.wallet.platform.database.data.dao.DaoProvider
import ch.admin.foitt.wallet.platform.database.domain.model.ActivityActorDisplayWithImage
import ch.admin.foitt.wallet.platform.utils.catchAndMap
import com.github.michaelbull.result.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

class ActivityActorDisplayWithImageRepositoryImpl @Inject constructor(
    daoProvider: DaoProvider,
) : ActivityActorDisplayWithImageRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getActorDisplaysWithImageByActivityIdFlow(
        activityId: Long
    ): Flow<Result<List<ActivityActorDisplayWithImage>, ActivityActorDisplayWithImageRepositoryError>> = daoFlow.flatMapLatest { dao ->
        dao?.getActorDisplaysByActivityId(activityId)
            ?.catchAndMap { throwable ->
                throwable.toActivityActorDisplayWithImageRepositoryError("error fetching ActivityActorDisplaysWithImage by activityId")
            } ?: emptyFlow()
    }

    private val daoFlow = daoProvider.activityActorDisplayWithImageDao
}
