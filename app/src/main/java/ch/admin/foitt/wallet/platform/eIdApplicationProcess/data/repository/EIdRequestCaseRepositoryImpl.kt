package ch.admin.foitt.wallet.platform.eIdApplicationProcess.data.repository

import ch.admin.foitt.wallet.platform.database.data.dao.DaoProvider
import ch.admin.foitt.wallet.platform.database.data.dao.EIdRequestCaseDao
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestCase
import ch.admin.foitt.wallet.platform.di.IoDispatcher
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.toEIdRequestCaseRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestCaseRepository
import ch.admin.foitt.wallet.platform.utils.suspendUntilNonNull
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EIdRequestCaseRepositoryImpl @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    daoProvider: DaoProvider,
) : EIdRequestCaseRepository {
    override suspend fun saveEIdRequestCase(
        case: EIdRequestCase
    ): Result<Unit, EIdRequestCaseRepositoryError> = withContext(ioDispatcher) {
        runSuspendCatching {
            eIdRequestCaseDao().insert(case)
        }.mapError { throwable ->
            throwable.toEIdRequestCaseRepositoryError("EIdRequestCaseRepository error")
        }
    }

    private val eIdRequestCaseDaoFlow = daoProvider.eIdRequestCaseDaoFlow
    private suspend fun eIdRequestCaseDao(): EIdRequestCaseDao = suspendUntilNonNull { eIdRequestCaseDaoFlow.value }
}
