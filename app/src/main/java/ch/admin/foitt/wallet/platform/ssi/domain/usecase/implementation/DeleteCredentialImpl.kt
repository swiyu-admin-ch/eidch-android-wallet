package ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialWithKeyBindingRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.DeleteCredentialError
import ch.admin.foitt.wallet.platform.ssi.domain.model.toDeleteCredentialError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialRepo
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialWithBundleItemsWithKeyBindingRepository
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteCredential
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteKeyStoreEntry
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class DeleteCredentialImpl @Inject constructor(
    private val credentialRepo: CredentialRepo,
    private val deleteKeyStoreEntry: DeleteKeyStoreEntry,
    private val verifiableCredentialWithBundleItemsWithKeyBindingRepository: VerifiableCredentialWithBundleItemsWithKeyBindingRepository,
) : DeleteCredential {
    override suspend fun invoke(credentialId: Long): Result<Unit, DeleteCredentialError> = coroutineBinding {
        val credentialWithBundleItemsWithKeyBinding =
            verifiableCredentialWithBundleItemsWithKeyBindingRepository.getByCredentialId(credentialId)
                .mapError(CredentialWithKeyBindingRepositoryError::toDeleteCredentialError)
                .bind()

        credentialWithBundleItemsWithKeyBinding.bundleItemsWithKeyBinding.forEach { bundleItemWithKeyBinding ->
            bundleItemWithKeyBinding.keyBinding?.let {
                deleteKeyStoreEntry(it.id)
            }
        }

        credentialRepo.deleteById(credentialWithBundleItemsWithKeyBinding.credential.id)
            .mapError(CredentialRepositoryError::toDeleteCredentialError).bind()
    }
}
