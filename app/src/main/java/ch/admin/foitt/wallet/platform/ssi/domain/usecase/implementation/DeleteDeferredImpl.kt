package ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.DeferredCredentialRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.DeleteDeferredCredentialError
import ch.admin.foitt.wallet.platform.ssi.domain.model.toDeleteCredentialError
import ch.admin.foitt.wallet.platform.ssi.domain.model.toDeleteDeferredCredentialError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialRepo
import ch.admin.foitt.wallet.platform.ssi.domain.repository.DeferredCredentialRepository
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteDeferred
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteKeyStoreEntry
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class DeleteDeferredImpl @Inject constructor(
    private val credentialRepository: CredentialRepo,
    private val deleteKeyStoreEntry: DeleteKeyStoreEntry,
    private val deferredCredentialRepository: DeferredCredentialRepository,
) : DeleteDeferred {
    override suspend fun invoke(credentialId: Long): Result<Unit, DeleteDeferredCredentialError> = coroutineBinding {
        val deferredCredentialWithWithKeyBinding =
            deferredCredentialRepository.getById(credentialId)
                .mapError(DeferredCredentialRepositoryError::toDeleteDeferredCredentialError)
                .bind()

        deferredCredentialWithWithKeyBinding.keyBindings.forEach { keybinding ->
            deleteKeyStoreEntry(keybinding.id)
        }

        credentialRepository.deleteById(credentialId)
            .mapError(CredentialRepositoryError::toDeleteCredentialError).bind()
    }
}
