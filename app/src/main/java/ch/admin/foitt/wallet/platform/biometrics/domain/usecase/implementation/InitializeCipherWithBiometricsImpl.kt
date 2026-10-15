package ch.admin.foitt.wallet.platform.biometrics.domain.usecase.implementation

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.model.BiometricAuthenticationError
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.model.BiometricPromptWrapper
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.usecase.LaunchBiometricPrompt
import ch.admin.foitt.wallet.platform.biometrics.domain.model.InitializeCipherWithBiometricsError
import ch.admin.foitt.wallet.platform.biometrics.domain.model.ResetBiometricsError
import ch.admin.foitt.wallet.platform.biometrics.domain.model.toInitializeCipherWithBiometricsError
import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.InitializeCipherWithBiometrics
import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.ResetBiometrics
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.model.GetCipherForEncryptionError
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.usecase.GetCipherForEncryption
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PassphraseStorageKeyConfig
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.crypto.Cipher
import javax.inject.Inject

class InitializeCipherWithBiometricsImpl @Inject constructor(
    private val getCipherForEncryption: GetCipherForEncryption,
    private val launchBiometricPrompt: LaunchBiometricPrompt,
    private val passphraseStorageKeyConfig: PassphraseStorageKeyConfig,
    private val resetBiometrics: ResetBiometrics,
) : InitializeCipherWithBiometrics {
    @CheckResult
    override suspend fun invoke(
        promptWrapper: BiometricPromptWrapper,
    ): Result<Cipher, InitializeCipherWithBiometricsError> = coroutineBinding {
        resetBiometrics().mapError(
            ResetBiometricsError::toInitializeCipherWithBiometricsError
        ).bind()
        val encryptionCipher: Cipher = getCipherForEncryption(
            keystoreKeyConfig = passphraseStorageKeyConfig,
            initializationVector = null,
        ).mapError(
            GetCipherForEncryptionError::toInitializeCipherWithBiometricsError
        ).bind()

        // If biometric fails for any reason, we abort the process
        launchBiometricPrompt(
            cipher = encryptionCipher,
            promptWrapper = promptWrapper,
        ).mapError(
            BiometricAuthenticationError::toInitializeCipherWithBiometricsError
        ).bind()
    }
}
