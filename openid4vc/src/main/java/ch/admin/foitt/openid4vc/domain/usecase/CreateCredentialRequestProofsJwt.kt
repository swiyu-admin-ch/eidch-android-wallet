package ch.admin.foitt.openid4vc.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialRequestProofsJwt
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchVerifiableCredentialError
import ch.admin.foitt.openid4vc.domain.model.keyBinding.BindingKeyPair
import com.github.michaelbull.result.Result

internal interface CreateCredentialRequestProofsJwt {
    @CheckResult
    suspend operator fun invoke(
        keyPairs: List<BindingKeyPair>,
        issuer: String,
        cNonce: String?,
    ): Result<CredentialRequestProofsJwt, FetchVerifiableCredentialError>
}
