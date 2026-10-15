package ch.admin.foitt.wallet.platform.login.domain.usecase.implementation

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.model.BiometricAuthenticationError
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.model.BiometricPromptWrapper
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.usecase.LaunchBiometricPrompt
import ch.admin.foitt.wallet.platform.biometrics.domain.model.GetBiometricsCipherError
import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.GetBiometricsCipher
import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.ResetBiometrics
import ch.admin.foitt.wallet.platform.database.domain.model.OpenDatabaseError
import ch.admin.foitt.wallet.platform.database.domain.usecase.OpenAppDatabase
import ch.admin.foitt.wallet.platform.login.domain.model.LoginError
import ch.admin.foitt.wallet.platform.login.domain.model.LoginWithBiometricsError
import ch.admin.foitt.wallet.platform.login.domain.model.toLoginWithBiometricsError
import ch.admin.foitt.wallet.platform.login.domain.usecase.LoginWithBiometrics
import ch.admin.foitt.wallet.platform.passphrase.domain.model.LoadAndDecryptPassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.LoadAndDecryptPassphrase
import ch.admin.foitt.wallet.platform.userInteraction.domain.usecase.UserInteraction
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onErr
import timber.log.Timber
import javax.crypto.Cipher
import javax.inject.Inject

class LoginWithBiometricsImpl @Inject constructor(
    private val loadAndDecryptPassphrase: LoadAndDecryptPassphrase,
    private val launchBiometricPrompt: LaunchBiometricPrompt,
    private val getBiometricsCipher: GetBiometricsCipher,
    private val openAppDatabase: OpenAppDatabase,
    private val userInteraction: UserInteraction,
    private val resetBiometrics: ResetBiometrics,
) : LoginWithBiometrics {
    @CheckResult
    override suspend fun invoke(promptWrapper: BiometricPromptWrapper): Result<Unit, LoginWithBiometricsError> {
        val result: Result<Unit, LoginWithBiometricsError> = coroutineBinding {
            val decryptionCipher: Cipher = getBiometricsCipher().mapError(
                GetBiometricsCipherError::toLoginWithBiometricsError
            ).bind()

            val initializedDecryptionCipher: Cipher = launchBiometricPrompt(
                promptWrapper = promptWrapper,
                cipher = decryptionCipher,
            ).mapError(
                BiometricAuthenticationError::toLoginWithBiometricsError
            ).bind()

            val decryptedPassphrase = loadAndDecryptPassphrase(
                decryptionCipher = initializedDecryptionCipher
            ).mapError(
                LoadAndDecryptPassphraseError::toLoginWithBiometricsError
            ).bind()

            openAppDatabase(
                passphrase = decryptedPassphrase
            ).onErr {
                Timber.d("AppDatabase login failed")
            }.mapError(OpenDatabaseError::toLoginWithBiometricsError).bind()

            Timber.d("Biometric Authentication succeeded")
        }

        // completing the biometric prompt does apparently not count as a user interaction
        // -> trigger it manually
        userInteraction()

        return result.onErr { loginWithBiometricsError ->
            onLoginFailure(loginWithBiometricsError)
        }
    }

    private suspend fun onLoginFailure(loginError: LoginWithBiometricsError) {
        when (loginError) {
            is LoginError.InvalidPassphrase -> {
                resetBiometrics()
            }

            is LoginError.BiometricsChanged -> {
                resetBiometrics()
            }

            is LoginError.General -> {
                resetBiometrics()
            }

            is LoginError.Cancelled -> {}
            is LoginError.BiometricsLocked -> {}
        }
    }
}
