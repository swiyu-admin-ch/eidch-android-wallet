package ch.admin.foitt.wallet.platform.credential.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.wallet.platform.credential.domain.model.UpdateDeferredCredentialError
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialWithAuthenticationAndKeyBinding
import com.github.michaelbull.result.Result

internal fun interface UpdateDeferredCredential {
    suspend operator fun invoke(
        deferredCredentialEntity: DeferredCredentialWithAuthenticationAndKeyBinding,
        credentialResponse: CredentialResponse.DeferredCredential,
        rawAndParsedIssuerCredentialInfo: RawAndParsedIssuerCredentialInfo,
    ): Result<Unit, UpdateDeferredCredentialError>
}
