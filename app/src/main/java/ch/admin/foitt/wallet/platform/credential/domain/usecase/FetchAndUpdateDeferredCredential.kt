package ch.admin.foitt.wallet.platform.credential.domain.usecase

import ch.admin.foitt.wallet.platform.credential.domain.model.FetchAndUpdateDeferredCredentialError
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialWithKeyBinding
import com.github.michaelbull.result.Result

fun interface FetchAndUpdateDeferredCredential {
    suspend operator fun invoke(
        deferredCredentialEntity: DeferredCredentialWithKeyBinding,
    ): Result<Unit, FetchAndUpdateDeferredCredentialError>
}
