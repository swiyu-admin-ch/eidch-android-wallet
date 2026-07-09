package ch.admin.foitt.wallet.platform.credential.domain.usecase

import ch.admin.foitt.wallet.platform.credential.domain.model.FetchAndUpdateDeferredCredentialError
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialWithAuthenticationAndKeyBinding
import com.github.michaelbull.result.Result

fun interface FetchAndUpdateDeferredCredential {
    suspend operator fun invoke(
        deferredCredentialEntity: DeferredCredentialWithAuthenticationAndKeyBinding,
    ): Result<Unit, FetchAndUpdateDeferredCredentialError>
}
