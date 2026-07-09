package ch.admin.foitt.openid4vc.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.GenerateDPoPKeyPairError
import ch.admin.foitt.openid4vc.domain.model.VerifiableCredentialParams
import ch.admin.foitt.openid4vc.domain.model.keyBinding.BindingKeyPair
import com.github.michaelbull.result.Result

fun interface GenerateDPoPKeyPair {
    suspend operator fun invoke(
        verifiableCredentialParams: VerifiableCredentialParams
    ): Result<BindingKeyPair?, GenerateDPoPKeyPairError>
}
