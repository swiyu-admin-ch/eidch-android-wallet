package ch.admin.foitt.wallet.platform.ssi.domain.usecase

import ch.admin.foitt.wallet.platform.ssi.domain.model.DeleteDeferredCredentialError
import com.github.michaelbull.result.Result

interface DeleteDeferred {
    suspend operator fun invoke(credentialId: Long): Result<Unit, DeleteDeferredCredentialError>
}
