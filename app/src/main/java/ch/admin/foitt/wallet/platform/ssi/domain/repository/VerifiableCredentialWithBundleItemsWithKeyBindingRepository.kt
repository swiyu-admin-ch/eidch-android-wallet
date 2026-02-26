package ch.admin.foitt.wallet.platform.ssi.domain.repository

import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialWithBundleItemsWithKeyBinding
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialWithKeyBindingRepositoryError
import com.github.michaelbull.result.Result

interface VerifiableCredentialWithBundleItemsWithKeyBindingRepository {
    suspend fun getAll(): Result<List<VerifiableCredentialWithBundleItemsWithKeyBinding>, CredentialWithKeyBindingRepositoryError>
    suspend fun getByCredentialId(
        credentialId: Long
    ): Result<VerifiableCredentialWithBundleItemsWithKeyBinding, CredentialWithKeyBindingRepositoryError>
}
