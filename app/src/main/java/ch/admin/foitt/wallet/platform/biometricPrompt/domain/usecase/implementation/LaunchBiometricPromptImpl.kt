package ch.admin.foitt.wallet.platform.biometricPrompt.domain.usecase.implementation

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.model.BiometricAuthenticationError
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.model.BiometricManagerResult
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.model.BiometricPromptWrapper
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.usecase.BiometricsStatus
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.usecase.LaunchBiometricPrompt
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import javax.crypto.Cipher
import javax.inject.Inject

class LaunchBiometricPromptImpl @Inject constructor(
    private val biometricsStatus: BiometricsStatus,
) : LaunchBiometricPrompt {
    @CheckResult
    override suspend operator fun invoke(
        promptWrapper: BiometricPromptWrapper,
        cipher: Cipher,
    ): Result<Cipher, BiometricAuthenticationError> {
        if (biometricsStatus() != BiometricManagerResult.Available) {
            return Err(BiometricAuthenticationError.createGeneralError(("Cannot use biometrics")))
        }
        return promptWrapper.launchPrompt(
            cipher = cipher,
        )
    }
}
