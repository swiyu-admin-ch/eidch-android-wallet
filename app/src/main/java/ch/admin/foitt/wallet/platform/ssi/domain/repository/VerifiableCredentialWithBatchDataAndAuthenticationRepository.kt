package ch.admin.foitt.wallet.platform.ssi.domain.repository

import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialWithBatchDataAndAuthentication
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialWithBatchDataAndAuthenticationRepositoryError
import com.github.michaelbull.result.Result

interface VerifiableCredentialWithBatchDataAndAuthenticationRepository {
    suspend fun getAll():
        Result<List<VerifiableCredentialWithBatchDataAndAuthentication>, CredentialWithBatchDataAndAuthenticationRepositoryError>
}
