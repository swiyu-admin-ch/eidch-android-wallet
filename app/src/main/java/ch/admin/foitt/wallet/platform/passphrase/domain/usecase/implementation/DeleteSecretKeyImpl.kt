package ch.admin.foitt.wallet.platform.passphrase.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.passphrase.domain.model.DeleteSecretKeyError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PassphraseStorageKeyConfig
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.DeleteSecretKey
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import java.security.KeyStore
import javax.inject.Inject

class DeleteSecretKeyImpl @Inject constructor(
    private val passphraseStorageKeyConfig: PassphraseStorageKeyConfig,
) : DeleteSecretKey {
    override fun invoke(): Result<Unit, DeleteSecretKeyError> = runSuspendCatching {
        val keyStore = KeyStore.getInstance(passphraseStorageKeyConfig.keystoreName)
        keyStore.load(null)
        keyStore.deleteEntry(passphraseStorageKeyConfig.encryptionKeyAlias)
    }.mapError { throwable ->
        PassphraseError.createGeneralError(throwable)
    }
}
