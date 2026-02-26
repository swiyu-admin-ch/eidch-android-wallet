package ch.admin.foitt.wallet.platform.ssi.data.repository

import ch.admin.foitt.wallet.platform.database.data.dao.DaoProvider
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialWithDisplays
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialWithDisplaysRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.DeferredCredentialWithDisplaysRepository
import ch.admin.foitt.wallet.platform.utils.catchAndMap
import com.github.michaelbull.result.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class DeferredCredentialWithDisplaysRepositoryImpl @Inject constructor(
    daoProvider: DaoProvider,
) : DeferredCredentialWithDisplaysRepository {

    override fun getAllFlow(): Flow<Result<List<DeferredCredentialWithDisplays>, CredentialWithDisplaysRepositoryError>> =
        daoFlow.flatMapLatest { dao ->
            dao?.getAll()?.catchAndMap {
                SsiError.Unexpected(it)
            } ?: emptyFlow()
        }

    private val daoFlow = daoProvider.deferredCredentialWithDisplaysDao
}
