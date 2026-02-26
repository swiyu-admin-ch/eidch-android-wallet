package ch.admin.foitt.wallet.platform.activityList.data.repository

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityWithDetailsRepositoryError
import ch.admin.foitt.wallet.platform.activityList.domain.model.toActivityWithDetailsRepositoryError
import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityWithDetailsRepository
import ch.admin.foitt.wallet.platform.database.data.dao.DaoProvider
import ch.admin.foitt.wallet.platform.database.domain.model.ActivityWithDetails
import ch.admin.foitt.wallet.platform.utils.catchAndMap
import com.github.michaelbull.result.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class ActivityWithDetailsRepositoryImpl @Inject constructor(
    daoProvider: DaoProvider,
) : ActivityWithDetailsRepository {
    override fun getNullableByIdFlow(
        activityId: Long
    ): Flow<Result<ActivityWithDetails?, ActivityWithDetailsRepositoryError>> = daoFlow.flatMapLatest { dao ->
        dao?.getNullableByIdFlow(activityId)
            ?.catchAndMap { throwable ->
                throwable.toActivityWithDetailsRepositoryError("Error getting ActivityWithDetails? by activityId")
            } ?: emptyFlow()
    }

    override fun getByIdFlow(
        activityId: Long
    ): Flow<Result<ActivityWithDetails, ActivityWithDetailsRepositoryError>> = daoFlow.flatMapLatest { dao ->
        dao?.getByIdFlow(activityId)
            ?.catchAndMap { throwable ->
                throwable.toActivityWithDetailsRepositoryError("Error getting ActivityWithDetails by activityId")
            } ?: emptyFlow()
    }

    private val daoFlow = daoProvider.activityWithDetailsDao
}
