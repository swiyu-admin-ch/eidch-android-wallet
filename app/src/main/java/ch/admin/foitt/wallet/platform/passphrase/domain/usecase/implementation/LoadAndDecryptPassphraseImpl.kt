package ch.admin.foitt.wallet.platform.passphrase.domain.usecase.implementation

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.passphrase.domain.model.LoadAndDecryptPassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.repository.PassphraseRepository
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.LoadAndDecryptPassphrase
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import javax.crypto.Cipher
import javax.inject.Inject

internal class LoadAndDecryptPassphraseImpl @Inject constructor(
    private val passphraseRepository: PassphraseRepository,
) : LoadAndDecryptPassphrase {
    @CheckResult
    override suspend operator fun invoke(decryptionCipher: Cipher): Result<ByteArray, LoadAndDecryptPassphraseError> = runSuspendCatching {
        val ciphertext = passphraseRepository.getPassphrase().ciphertext
        val plainTextBytes = decryptionCipher.doFinal(ciphertext)
        plainTextBytes
    }.mapError { throwable ->
        PassphraseError.createGeneralError(throwable)
    }
}
