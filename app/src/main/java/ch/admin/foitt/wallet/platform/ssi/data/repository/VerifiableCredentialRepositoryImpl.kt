package ch.admin.foitt.wallet.platform.ssi.data.repository

import ch.admin.foitt.wallet.platform.database.data.dao.DaoProvider
import ch.admin.foitt.wallet.platform.database.data.dao.VerifiableCredentialDao
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialEntity
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableProgressionState
import ch.admin.foitt.wallet.platform.di.IoDispatcher
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.model.VerifiableCredentialRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialRepository
import ch.admin.foitt.wallet.platform.utils.suspendUntilNonNull
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject

class VerifiableCredentialRepositoryImpl @Inject constructor(
    daoProvider: DaoProvider,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : VerifiableCredentialRepository {
    override suspend fun getAllIds(): Result<List<Long>, VerifiableCredentialRepositoryError> = runSuspendCatching {
        withContext(ioDispatcher) {
            verifiableCredentialDao().getAllIds()
        }
    }.mapError { throwable ->
        Timber.e(t = throwable, message = "Failed to get all verifiable credential ids")
        SsiError.Unexpected(throwable)
    }

    override suspend fun getById(id: Long): Result<VerifiableCredentialEntity, VerifiableCredentialRepositoryError> = runSuspendCatching {
        withContext(ioDispatcher) {
            verifiableCredentialDao().getById(id)
        }
    }.mapError { throwable ->
        Timber.e(throwable)
        SsiError.Unexpected(throwable)
    }

    override suspend fun onBundleItemUpdate(id: Long): Result<Int, VerifiableCredentialRepositoryError> = runSuspendCatching {
        withContext(ioDispatcher) {
            verifiableCredentialDao().updatedAt(
                id = id,
                updatedAt = Instant.now().epochSecond,
            )
        }
    }.mapError { throwable ->
        Timber.e(throwable)
        SsiError.Unexpected(throwable)
    }

    override suspend fun updateProgressionStateByCredentialId(
        credentialId: Long,
        progressionState: VerifiableProgressionState,
    ): Result<Int, VerifiableCredentialRepositoryError> = runSuspendCatching {
        withContext(ioDispatcher) {
            verifiableCredentialDao().updateProgressStateByCredentialId(
                id = credentialId,
                progressionState = progressionState,
            )
        }
    }.mapError { throwable ->
        Timber.e(throwable)
        SsiError.Unexpected(throwable)
    }

    private suspend fun verifiableCredentialDao(): VerifiableCredentialDao = suspendUntilNonNull {
        verifiableCredentialDaoFlow.value
    }

    private val verifiableCredentialDaoFlow = daoProvider.verifiableCredentialDaoFlow
}
