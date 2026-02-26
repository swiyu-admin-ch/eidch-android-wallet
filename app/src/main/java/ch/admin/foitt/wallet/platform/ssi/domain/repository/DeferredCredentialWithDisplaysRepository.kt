package ch.admin.foitt.wallet.platform.ssi.domain.repository

import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialWithDisplays
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialWithDisplaysRepositoryError
import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow

interface DeferredCredentialWithDisplaysRepository {
    fun getAllFlow(): Flow<Result<List<DeferredCredentialWithDisplays>, CredentialWithDisplaysRepositoryError>>
}
