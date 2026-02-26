package ch.admin.foitt.wallet.platform.ssi.domain.repository

import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialRepositoryError
import com.github.michaelbull.result.Result

interface CredentialRepo {
    suspend fun getAll(): Result<List<Credential>, CredentialRepositoryError>
    suspend fun getById(id: Long): Result<Credential, CredentialRepositoryError>
    suspend fun deleteById(id: Long): Result<Unit, CredentialRepositoryError>
}
