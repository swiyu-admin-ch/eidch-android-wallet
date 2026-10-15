package ch.admin.foitt.wallet.platform.biometrics

import android.annotation.SuppressLint
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.model.BiometricAuthenticationError
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.model.BiometricPromptWrapper
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.usecase.LaunchBiometricPrompt
import ch.admin.foitt.wallet.platform.biometrics.domain.model.BiometricsError
import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.InitializeCipherWithBiometrics
import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.ResetBiometrics
import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.implementation.InitializeCipherWithBiometricsImpl
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.model.KeystoreError
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.usecase.GetCipherForEncryption
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PassphraseStorageKeyConfig
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.crypto.Cipher

class InitializeCipherWithBiometricsImplTest {

    @MockK
    private lateinit var mockGetCipherForEncryption: GetCipherForEncryption

    @MockK
    private lateinit var mockLaunchBiometricPrompt: LaunchBiometricPrompt

    @MockK
    private lateinit var mockCipher: Cipher

    @MockK
    private lateinit var mockPassphraseStorageKeyConfig: PassphraseStorageKeyConfig

    @MockK
    private lateinit var mockPromptWrapper: BiometricPromptWrapper

    @MockK
    lateinit var mockResetBiometrics: ResetBiometrics

    private lateinit var useCase: InitializeCipherWithBiometrics

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        useCase = InitializeCipherWithBiometricsImpl(
            getCipherForEncryption = mockGetCipherForEncryption,
            launchBiometricPrompt = mockLaunchBiometricPrompt,
            passphraseStorageKeyConfig = mockPassphraseStorageKeyConfig,
            resetBiometrics = mockResetBiometrics,
        )

        // Sunny path by default
        coEvery { mockGetCipherForEncryption(any(), any()) } returns Ok(mockCipher)
        coEvery { mockLaunchBiometricPrompt(any(), any()) } returns Ok(mockCipher)
        coEvery { mockResetBiometrics() } returns Ok(Unit)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @SuppressLint("CheckResult")
    @Test
    fun `A successful prompt follows specific steps and returns a cipher`() = runTest {
        val result = useCase(mockPromptWrapper).assertOk()

        assertEquals(mockCipher, result)

        coVerifyOrder {
            mockResetBiometrics.invoke()
            mockGetCipherForEncryption.invoke(any(), any())
            mockLaunchBiometricPrompt.invoke(any(), any())
        }
    }

    @Test
    fun `A failure while resetting the biometric state should return an init error`() = runTest {
        coEvery { mockResetBiometrics() } returns Err(BiometricsError.createGeneralError())

        useCase(mockPromptWrapper).assertErrorType(BiometricsError.General::class)
    }

    @Test
    fun `A cipher exception should return an init error`() = runTest {
        val cipherError = KeystoreError.createGeneralError()
        coEvery { mockGetCipherForEncryption(any(), any()) } returns Err(cipherError)

        useCase(mockPromptWrapper).assertErrorType(BiometricsError.General::class)
    }

    @Test
    fun `A biometric prompt cancellation should return a cancelled biometric init`() = runTest {
        coEvery {
            mockLaunchBiometricPrompt(any(), any())
        } returns Err(BiometricAuthenticationError.createPromptCancelledError())

        useCase(mockPromptWrapper).assertErrorType(BiometricsError.Cancelled::class)
    }

    @Test
    fun `A biometric prompt failure should return enabling biometrics failed`() = runTest {
        coEvery {
            mockLaunchBiometricPrompt(any(), any())
        } returns Err(BiometricAuthenticationError.createPromptFailureError(""))

        useCase(mockPromptWrapper).assertErrorType(BiometricsError.General::class)
    }

    @Test
    fun `A biometric prompt error should return enabling biometrics error`() = runTest {
        coEvery {
            mockLaunchBiometricPrompt(any(), any())
        } returns Err(BiometricAuthenticationError.createGeneralError(Exception()))

        useCase(mockPromptWrapper).assertErrorType(BiometricsError.General::class)
    }
}
