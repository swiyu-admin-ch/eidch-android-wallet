package ch.admin.foitt.wallet.feature.changePassphrase.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.changePassphrase.domain.model.ChangePassphraseError
import ch.admin.foitt.wallet.feature.changePassphrase.domain.model.toChangePassphraseError
import ch.admin.foitt.wallet.feature.changePassphrase.domain.usecase.ChangePassphrase
import ch.admin.foitt.wallet.platform.appSetupState.domain.repository.UseBiometricLoginRepository
import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.ResetBiometrics
import ch.admin.foitt.wallet.platform.database.domain.model.ChangeDatabasePassphraseError
import ch.admin.foitt.wallet.platform.database.domain.usecase.ChangeDatabasePassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.model.HashPassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PepperPassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.repository.PepperIvRepository
import ch.admin.foitt.wallet.platform.passphrase.domain.repository.SaltRepository
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.HashPassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.PepperPassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.SavePassphraseWasDeleted
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class ChangePassphraseImpl @Inject constructor(
    private val hashPassphrase: HashPassphrase,
    private val saltRepository: SaltRepository,
    private val pepperPassphrase: PepperPassphrase,
    private val pepperIvRepository: PepperIvRepository,
    private val changeDatabasePassphrase: ChangeDatabasePassphrase,
    private val useBiometricLoginRepository: UseBiometricLoginRepository,
    private val resetBiometrics: ResetBiometrics,
    private val savePassphraseWasDeleted: SavePassphraseWasDeleted,
) : ChangePassphrase {
    override suspend fun invoke(newPassphrase: String): Result<Unit, ChangePassphraseError> = coroutineBinding {
        val passphraseHash = hashPassphrase(
            passphrase = newPassphrase,
            initializeSalt = true,
        ).mapError(HashPassphraseError::toChangePassphraseError).bind()

        val passphraseHashPeppered = pepperPassphrase(
            passphrase = passphraseHash.hash,
            initializePepper = true,
        ).mapError(PepperPassphraseError::toChangePassphraseError).bind()

        changeDatabasePassphrase(
            newPassphrase = passphraseHashPeppered.hash,
        ).mapError(ChangeDatabasePassphraseError::toChangePassphraseError).bind()

        saltRepository.save(passphraseHash.salt)
        pepperIvRepository.save(passphraseHashPeppered.initializationVector)

        // Biometric login will not work anymore
        val wasBiometricsEnabled = useBiometricLoginRepository.getUseBiometricLogin()
        resetBiometrics()
        savePassphraseWasDeleted(wasBiometricsEnabled)
    }
}
