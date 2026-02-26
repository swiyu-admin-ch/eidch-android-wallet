package ch.admin.foitt.wallet.platform.ssi.data.repository

import ch.admin.foitt.wallet.platform.database.data.dao.DaoProvider
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialWithKeyBinding
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredProgressionState
import ch.admin.foitt.wallet.platform.di.IoDispatcher
import ch.admin.foitt.wallet.platform.ssi.domain.model.DeferredCredentialRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.DeferredCredentialRepository
import ch.admin.foitt.wallet.platform.utils.suspendUntilNonNull
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class DeferredCredentialRepositoryImpl @Inject constructor(
    daoProvider: DaoProvider,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : DeferredCredentialRepository {

    override suspend fun getAll(): Result<List<DeferredCredentialWithKeyBinding>, DeferredCredentialRepositoryError> = runSuspendCatching {
        withContext(ioDispatcher) {
            dao().getAll()
        }
    }.mapError { throwable ->
        Timber.e(t = throwable, message = "Failed to get all deferred credentials")
        SsiError.Unexpected(throwable)
    }

    override suspend fun updateStatus(
        credentialId: Long,
        progressionState: DeferredProgressionState,
        polledAt: Long,
        pollInterval: Int,
    ): Result<Int, DeferredCredentialRepositoryError> = runSuspendCatching {
        withContext(ioDispatcher) {
            dao().updateStatusByCredentialId(
                credentialId = credentialId,
                progressionState = progressionState,
                polledAt = polledAt,
                pollInterval = pollInterval,
            )
        }
    }.mapError { throwable ->
        Timber.e(t = throwable, message = "Failed to update deferred credential")
        SsiError.Unexpected(throwable)
    }

    private suspend fun dao() = suspendUntilNonNull { daoFlow.value }
    private val daoFlow = daoProvider.deferredCredentialDao
}
