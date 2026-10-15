package ch.admin.foitt.wallet.platform.passphrase.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.model.GetCipherForEncryptionError
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.usecase.GetCipherForEncryption
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PassphrasePepperKeyConfig
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PepperPassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PepperedData
import ch.admin.foitt.wallet.platform.passphrase.domain.model.toPepperPassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.repository.PepperIvRepository
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.PepperPassphrase
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import javax.inject.Inject

internal class PepperPassphraseImpl @Inject constructor(
    private val getCipherForEncryption: GetCipherForEncryption,
    private val pepperIvRepository: PepperIvRepository,
    private val passphrasePepperKeyConfig: PassphrasePepperKeyConfig,
) : PepperPassphrase {
    override suspend fun invoke(
        passphrase: ByteArray,
        initializePepper: Boolean,
    ): Result<PepperedData, PepperPassphraseError> {
        return coroutineBinding {
            val pepperInitializationVector: ByteArray? = if (!initializePepper) {
                pepperIvRepository.get()
            } else {
                null
            }

            val pepperCipher = getCipherForEncryption(
                keystoreKeyConfig = passphrasePepperKeyConfig,
                initializationVector = pepperInitializationVector,
            ).mapError(GetCipherForEncryptionError::toPepperPassphraseError).bind()

            val passphraseBytes: ByteArray = runSuspendCatching {
                pepperCipher.doFinal(passphrase)
            }.mapError { throwable ->
                PassphraseError.createGeneralError(throwable)
            }.bind()

            PepperedData(passphraseBytes, pepperCipher.iv)
        }
    }
}
