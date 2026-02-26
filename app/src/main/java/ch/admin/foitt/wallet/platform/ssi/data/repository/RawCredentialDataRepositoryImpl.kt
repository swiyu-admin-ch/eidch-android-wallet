package ch.admin.foitt.wallet.platform.ssi.data.repository

import ch.admin.foitt.wallet.platform.database.data.dao.DaoProvider
import ch.admin.foitt.wallet.platform.di.IoDispatcher
import ch.admin.foitt.wallet.platform.ssi.domain.model.RawCredentialDataRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.RawCredentialDataRepository
import ch.admin.foitt.wallet.platform.utils.suspendUntilNonNull
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class RawCredentialDataRepositoryImpl @Inject constructor(
    daoProvider: DaoProvider,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : RawCredentialDataRepository {
    override suspend fun getByCredentialId(credentialId: Long) = withContext(ioDispatcher) {
        runSuspendCatching {
            dao().getRawCredentialDataByCredentialId(credentialId = credentialId).first()
        }.mapError { throwable ->
            throwable.toRawCredentialDataRepositoryError("getByCredentialId failed")
        }
    }

    override suspend fun updateMetadataByCredentialId(
        credentialId: Long,
        metadata: ByteArray
    ): Result<Int, RawCredentialDataRepositoryError> = withContext(ioDispatcher) {
        runSuspendCatching {
            dao().updateMetadataByCredentialId(credentialId = credentialId, metadata = metadata)
        }.mapError { throwable ->
            throwable.toRawCredentialDataRepositoryError("getByCredentialId failed")
        }
    }

    private suspend fun dao() = suspendUntilNonNull { daoFlow.value }
    private val daoFlow = daoProvider.rawCredentialDataDao
}

private fun Throwable.toRawCredentialDataRepositoryError(message: String): RawCredentialDataRepositoryError {
    Timber.e(t = this, message = message)
    return SsiError.Unexpected(this)
}
