package ch.admin.foitt.wallet.platform.ssi.data.repository

import ch.admin.foitt.wallet.platform.database.data.dao.DaoProvider
import ch.admin.foitt.wallet.platform.database.data.dao.VerifiableCredentialWithBatchDataAndAuthenticationDao
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialWithBatchDataAndAuthentication
import ch.admin.foitt.wallet.platform.di.IoDispatcher
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialWithBatchDataAndAuthenticationRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialWithBatchDataAndAuthenticationRepository
import ch.admin.foitt.wallet.platform.utils.suspendUntilNonNull
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class VerifiableCredentialWithBatchDataAndAuthenticationRepositoryImpl @Inject constructor(
    daoProvider: DaoProvider,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : VerifiableCredentialWithBatchDataAndAuthenticationRepository {
    override suspend fun getAll():
        Result<List<VerifiableCredentialWithBatchDataAndAuthentication>, CredentialWithBatchDataAndAuthenticationRepositoryError> =
        runSuspendCatching {
            withContext(ioDispatcher) {
                dao().getAll()
            }
        }.mapError { throwable ->
            Timber.e(throwable)
            SsiError.Unexpected(throwable)
        }

    private suspend fun dao(): VerifiableCredentialWithBatchDataAndAuthenticationDao = suspendUntilNonNull { daoFlow.value }
    private val daoFlow = daoProvider.verifiableCredentialWithBatchDataAndAuthenticationDaoFlow
}
