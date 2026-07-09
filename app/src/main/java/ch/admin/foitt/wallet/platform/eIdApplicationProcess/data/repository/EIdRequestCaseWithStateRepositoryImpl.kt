package ch.admin.foitt.wallet.platform.eIdApplicationProcess.data.repository

import ch.admin.foitt.wallet.platform.database.data.dao.DaoProvider
import ch.admin.foitt.wallet.platform.database.data.dao.EIdRequestCaseWithStateDao
import ch.admin.foitt.wallet.platform.di.IoDispatcher
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseWithState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseWithStateRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.toEIdRequestCaseWithStateRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestCaseWithStateRepository
import ch.admin.foitt.wallet.platform.utils.catchAndMap
import ch.admin.foitt.wallet.platform.utils.suspendUntilNonNull
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class EIdRequestCaseWithStateRepositoryImpl @Inject constructor(
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    daoProvider: DaoProvider,
) : EIdRequestCaseWithStateRepository {
    override fun getEIdRequestCasesWithStatesFlow(): Flow<Result<List<EIdRequestCaseWithState>, EIdRequestCaseWithStateRepositoryError>> =
        eIdRequestCaseWithStateDaoFlow.flatMapLatest { dao ->
            dao?.getEIdCasesWithStatesFlow()
                ?.catchAndMap { throwable ->
                    throwable.toEIdRequestCaseWithStateRepositoryError("getEIdRequestCasesWithStatesFlow error")
                } ?: emptyFlow()
        }

    override suspend fun getEIdRequestCasesWithStates(): Result<List<EIdRequestCaseWithState>, EIdRequestCaseWithStateRepositoryError> =
        withContext(ioDispatcher) {
            runSuspendCatching {
                eIdRequestCaseWithStateDao().getEIdCasesWithStates()
            }.mapError { throwable ->
                throwable.toEIdRequestCaseWithStateRepositoryError("getEIdRequestCasesWithStates error")
            }
        }

    private val eIdRequestCaseWithStateDaoFlow = daoProvider.eIdRequestCaseWithStateDaoFlow

    private suspend fun eIdRequestCaseWithStateDao(): EIdRequestCaseWithStateDao = suspendUntilNonNull {
        eIdRequestCaseWithStateDaoFlow.value
    }
}
