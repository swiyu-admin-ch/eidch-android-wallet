package ch.admin.foitt.wallet.platform.ssi.domain.repository

import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialEntity
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableProgressionState
import ch.admin.foitt.wallet.platform.ssi.domain.model.VerifiableCredentialRepositoryError
import com.github.michaelbull.result.Result

interface VerifiableCredentialRepository {
    suspend fun getAllIds(): Result<List<Long>, VerifiableCredentialRepositoryError>
    suspend fun getById(id: Long): Result<VerifiableCredentialEntity, VerifiableCredentialRepositoryError>
    suspend fun onBundleItemUpdate(id: Long): Result<Int, VerifiableCredentialRepositoryError>
    suspend fun updateProgressionStateByCredentialId(
        credentialId: Long,
        progressionState: VerifiableProgressionState
    ): Result<Int, VerifiableCredentialRepositoryError>
}
