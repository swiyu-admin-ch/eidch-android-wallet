package ch.admin.foitt.wallet.platform.passphrase.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.passphrase.domain.model.CiphertextWrapper
import ch.admin.foitt.wallet.platform.passphrase.domain.model.EncryptAndSavePassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.repository.PassphraseRepository
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.EncryptAndSavePassphrase
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import javax.crypto.Cipher
import javax.inject.Inject

internal class EncryptAndSavePassphraseImpl @Inject constructor(
    private val passphraseRepository: PassphraseRepository,
) : EncryptAndSavePassphrase {
    override suspend fun invoke(
        passphrase: ByteArray,
        encryptionCipher: Cipher
    ): Result<Unit, EncryptAndSavePassphraseError> = runSuspendCatching {
        val ciphertext = encryptionCipher.doFinal(passphrase)
        val cipherTextWrapper = CiphertextWrapper(
            ciphertext = ciphertext,
            initializationVector = encryptionCipher.iv,
        )
        passphraseRepository.savePassphrase(cipherTextWrapper)
    }.mapError { throwable ->
        PassphraseError.createGeneralError(throwable)
    }
}
