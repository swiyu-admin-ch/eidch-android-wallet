package ch.admin.foitt.wallet.platform.passphrase.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.di.IoDispatcher
import ch.admin.foitt.wallet.platform.passphrase.domain.model.EncryptAndSavePassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.HashPassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PepperPassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.StorePassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.toStorePassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.EncryptAndSavePassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.HashPassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.PepperPassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.StorePassphrase
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.crypto.Cipher
import javax.inject.Inject

class StorePassphraseImpl @Inject constructor(
    private val hashPassphrase: HashPassphrase,
    private val encryptAndSavePassphrase: EncryptAndSavePassphrase,
    private val pepperPassphrase: PepperPassphrase,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : StorePassphrase {
    override suspend fun invoke(
        passphrase: String,
        encryptionCipher: Cipher,
    ): Result<Unit, StorePassphraseError> = withContext(ioDispatcher) {
        coroutineBinding {
            val passphraseHash = hashPassphrase(
                passphrase = passphrase,
                initializeSalt = false,
            ).mapError(HashPassphraseError::toStorePassphraseError).bind()

            val passphraseHashPeppered = pepperPassphrase(
                passphrase = passphraseHash.hash,
                initializePepper = false,
            ).mapError(PepperPassphraseError::toStorePassphraseError).bind()

            Timber.d("Passphrase, CryptoObject present, encrypting passphrase")
            encryptAndSavePassphrase(
                passphraseHashPeppered.hash,
                encryptionCipher,
            ).mapError(EncryptAndSavePassphraseError::toStorePassphraseError).bind()
        }
    }
}
