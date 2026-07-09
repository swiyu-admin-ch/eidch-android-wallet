package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.GetKeyPairForKeyBindingError
import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.JWSKeyPair
import ch.admin.foitt.openid4vc.domain.model.keyBinding.BindingKeyPair
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import ch.admin.foitt.openid4vc.domain.usecase.GetKeyPairForKeyBinding
import ch.admin.foitt.wallet.platform.credential.domain.model.GetBindingKeyPairError
import ch.admin.foitt.wallet.platform.credential.domain.model.toGetBindingKeyPairError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetBindingKeyPair
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialAuthenticationWithDpopBinding
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

internal class GetBindingKeyPairImpl @Inject constructor(
    private val getKeyPairForKeyBinding: GetKeyPairForKeyBinding,
) : GetBindingKeyPair {
    override suspend fun invoke(
        authentication: CredentialAuthenticationWithDpopBinding,
    ): Result<BindingKeyPair?, GetBindingKeyPairError> = coroutineBinding {
        val keyBinding = authentication.dpopBinding?.let {
            KeyBinding(
                identifier = it.id,
                algorithm = SigningAlgorithm.valueOf(it.algorithm),
                bindingType = it.bindingType,
                publicKey = it.publicKey,
                privateKey = it.privateKey,
            )
        } ?: return@coroutineBinding null

        val keyPair = getKeyPairForKeyBinding(keyBinding)
            .mapError(GetKeyPairForKeyBindingError::toGetBindingKeyPairError)
            .bind()

        BindingKeyPair(
            keyPair = JWSKeyPair(
                algorithm = keyBinding.algorithm,
                keyPair = keyPair,
                keyId = keyBinding.identifier,
                bindingType = keyBinding.bindingType,
            ),
            attestationJwt = null,
        )
    }
}
