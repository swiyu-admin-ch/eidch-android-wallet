package ch.admin.foitt.wallet.platform.biometrics.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.model.BiometricPromptWrapper
import ch.admin.foitt.wallet.platform.biometrics.domain.model.InitializeCipherWithBiometricsError
import com.github.michaelbull.result.Result
import javax.crypto.Cipher

interface InitializeCipherWithBiometrics {
    @CheckResult
    suspend operator fun invoke(
        promptWrapper: BiometricPromptWrapper,
    ): Result<Cipher, InitializeCipherWithBiometricsError>
}
