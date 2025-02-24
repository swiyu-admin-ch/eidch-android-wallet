package ch.admin.foitt.wallet.platform.eIdApplicationProcess.data.repository

import ch.admin.foitt.wallet.platform.database.data.dao.DaoProvider
import ch.admin.foitt.wallet.platform.database.data.dao.EIdRequestStateDao
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestState
import ch.admin.foitt.wallet.platform.di.IoDispatcher
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestStateRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.toEIdRequestStateRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestStateRepository
import ch.admin.foitt.wallet.platform.utils.suspendUntilNonNull
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EIdRequestStateRepositoryImpl @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    daoProvider: DaoProvider,
) : EIdRequestStateRepository {
    override suspend fun saveEIdRequestState(
        state: EIdRequestState
    ): Result<Long, EIdRequestStateRepositoryError> = withContext(ioDispatcher) {
        runSuspendCatching {
            eIdRequestStateDao().insert(state)
        }.mapError(Throwable::toEIdRequestStateRepositoryError)
    }

    private val eIdRequestStateDaoFlow = daoProvider.eIdRequestStateDaoFlow
    private suspend fun eIdRequestStateDao(): EIdRequestStateDao = suspendUntilNonNull { eIdRequestStateDaoFlow.value }
}
