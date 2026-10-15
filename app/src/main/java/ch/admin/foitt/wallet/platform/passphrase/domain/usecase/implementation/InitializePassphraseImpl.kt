package ch.admin.foitt.wallet.platform.passphrase.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.database.domain.model.CreateAppDatabaseError
import ch.admin.foitt.wallet.platform.database.domain.usecase.CreateAppDatabase
import ch.admin.foitt.wallet.platform.di.IoDispatcher
import ch.admin.foitt.wallet.platform.passphrase.domain.model.EncryptAndSavePassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.HashPassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.InitializePassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PepperPassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.toInitializePassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.repository.PepperIvRepository
import ch.admin.foitt.wallet.platform.passphrase.domain.repository.SaltRepository
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.EncryptAndSavePassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.HashPassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.InitializePassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.PepperPassphrase
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.crypto.Cipher
import javax.inject.Inject

@Suppress("LongParameterList")
class InitializePassphraseImpl @Inject constructor(
    private val hashPassphrase: HashPassphrase,
    private val saltRepository: SaltRepository,
    private val pepperIvRepository: PepperIvRepository,
    private val encryptAndSavePassphrase: EncryptAndSavePassphrase,
    private val createAppDatabase: CreateAppDatabase,
    private val pepperPassphrase: PepperPassphrase,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : InitializePassphrase {
    override suspend fun invoke(
        passphrase: String,
        encryptionCipher: Cipher?,
    ): Result<Unit, InitializePassphraseError> = withContext(ioDispatcher) {
        coroutineBinding {
            val passphraseHash = hashPassphrase(
                passphrase = passphrase,
                initializeSalt = true,
            ).mapError(HashPassphraseError::toInitializePassphraseError).bind()

            val passphraseHashPeppered = pepperPassphrase(
                passphrase = passphraseHash.hash,
                initializePepper = true,
            ).mapError(PepperPassphraseError::toInitializePassphraseError).bind()

            createAppDatabase(
                passphrase = passphraseHashPeppered.hash,
            ).mapError(CreateAppDatabaseError::toInitializePassphraseError).bind()

            saltRepository.save(passphraseHash.salt)
            pepperIvRepository.save(passphraseHashPeppered.initializationVector)

            if (encryptionCipher != null) {
                encryptAndSavePassphrase(
                    passphrase = passphraseHashPeppered.hash,
                    encryptionCipher = encryptionCipher,
                ).mapError(EncryptAndSavePassphraseError::toInitializePassphraseError).bind()
            }
        }
    }
}
