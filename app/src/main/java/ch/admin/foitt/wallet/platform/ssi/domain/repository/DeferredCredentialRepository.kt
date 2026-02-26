package ch.admin.foitt.wallet.platform.ssi.domain.repository

import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialWithKeyBinding
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredProgressionState
import ch.admin.foitt.wallet.platform.ssi.domain.model.DeferredCredentialRepositoryError
import com.github.michaelbull.result.Result

interface DeferredCredentialRepository {
    suspend fun getAll(): Result<List<DeferredCredentialWithKeyBinding>, DeferredCredentialRepositoryError>

    suspend fun updateStatus(
        credentialId: Long,
        progressionState: DeferredProgressionState,
        polledAt: Long,
        pollInterval: Int,
    ): Result<Int, DeferredCredentialRepositoryError>
}
