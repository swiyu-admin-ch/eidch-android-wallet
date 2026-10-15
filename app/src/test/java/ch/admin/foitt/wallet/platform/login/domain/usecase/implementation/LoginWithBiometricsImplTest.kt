package ch.admin.foitt.wallet.platform.login.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.biometricPrompt.domain.model.BiometricAuthenticationError
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.model.BiometricPromptWrapper
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.usecase.LaunchBiometricPrompt
import ch.admin.foitt.wallet.platform.biometrics.domain.model.BiometricsError
import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.GetBiometricsCipher
import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.ResetBiometrics
import ch.admin.foitt.wallet.platform.database.domain.model.DatabaseError
import ch.admin.foitt.wallet.platform.database.domain.usecase.OpenAppDatabase
import ch.admin.foitt.wallet.platform.login.domain.model.LoginError
import ch.admin.foitt.wallet.platform.login.domain.usecase.LoginWithBiometrics
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.LoadAndDecryptPassphrase
import ch.admin.foitt.wallet.platform.userInteraction.domain.usecase.UserInteraction
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.crypto.Cipher

class LoginWithBiometricsImplTest {
    @MockK
    private lateinit var mockLoadAndDecryptPassphrase: LoadAndDecryptPassphrase

    @MockK
    private lateinit var mockLaunchBiometricPrompt: LaunchBiometricPrompt

    @MockK
    private lateinit var mockGetBiometricsCipher: GetBiometricsCipher

    @MockK
    private lateinit var mockOpenAppDatabase: OpenAppDatabase

    @MockK
    private lateinit var mockUserInteraction: UserInteraction

    @MockK
    private lateinit var mockResetBiometrics: ResetBiometrics

    @MockK
    private lateinit var mockBiometricPromptWrapper: BiometricPromptWrapper

    @MockK
    private lateinit var mockCipher: Cipher

    @MockK
    private lateinit var mockInitializedDecryptionCipher: Cipher

    private val mockDecryptedPassphrase = byteArrayOf()

    private lateinit var useCase: LoginWithBiometrics

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = LoginWithBiometricsImpl(
            loadAndDecryptPassphrase = mockLoadAndDecryptPassphrase,
            launchBiometricPrompt = mockLaunchBiometricPrompt,
            getBiometricsCipher = mockGetBiometricsCipher,
            openAppDatabase = mockOpenAppDatabase,
            userInteraction = mockUserInteraction,
            resetBiometrics = mockResetBiometrics,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Logging in successfully with biometrics returns an Ok`() = runTest {
        useCase(mockBiometricPromptWrapper).assertOk()

        coVerify(exactly = 0) {
            mockResetBiometrics()
        }
    }

    @Test
    fun `LoginWithBiometrics returns an error when biometrics changed`() = runTest {
        coEvery { mockGetBiometricsCipher() } returns Err(BiometricsError.createInvalidatedKeyError())
        useCase(mockBiometricPromptWrapper).assertErrorType(LoginError.BiometricsChanged::class)

        coVerify(exactly = 1) {
            mockResetBiometrics()
        }
    }

    @Test
    fun `LoginWithBiometrics returns an error when the biometrics prompt was cancelled by the user`() = runTest {
        coEvery {
            mockLaunchBiometricPrompt(mockBiometricPromptWrapper, mockCipher)
        } returns Err(BiometricAuthenticationError.createPromptCancelledError())

        useCase(mockBiometricPromptWrapper).assertErrorType(LoginError.Cancelled::class)

        coVerify(exactly = 0) {
            mockResetBiometrics()
        }
    }

    @Test
    fun `LoginWithBiometrics returns an error when biometrics are locked on the device`() = runTest {
        coEvery {
            mockLaunchBiometricPrompt(mockBiometricPromptWrapper, mockCipher)
        } returns Err(BiometricAuthenticationError.createPromptLockedError())

        useCase(mockBiometricPromptWrapper).assertErrorType(LoginError.BiometricsLocked::class)

        coVerify(exactly = 0) {
            mockResetBiometrics()
        }
    }

    @Test
    fun `LoginWithBiometrics returns an error when the passphrase for the DB is wrong`() = runTest {
        coEvery {
            mockOpenAppDatabase(mockDecryptedPassphrase)
        } returns Err(DatabaseError.createWrongPassphraseError(Exception()))

        useCase(mockBiometricPromptWrapper).assertErrorType(LoginError.InvalidPassphrase::class)

        coVerify(exactly = 1) {
            mockResetBiometrics()
        }
    }

    @Test
    fun `LoginWithBiometrics maps errors from getBiometricsCipher`() = runTest {
        coEvery { mockGetBiometricsCipher() } returns Err(BiometricsError.createGeneralError())

        useCase(mockBiometricPromptWrapper).assertErrorType(LoginError.General::class)

        coVerify(exactly = 1) {
            mockResetBiometrics()
        }
    }

    @Test
    fun `LoginWithBiometrics maps errors from launchBiometricPrompt`() = runTest {
        coEvery {
            mockLaunchBiometricPrompt(mockBiometricPromptWrapper, mockCipher)
        } returns Err(BiometricAuthenticationError.createGeneralError(Exception()))

        useCase(mockBiometricPromptWrapper).assertErrorType(LoginError.General::class)

        coVerify(exactly = 1) {
            mockResetBiometrics()
        }
    }

    @Test
    fun `LoginWithBiometrics maps errors from loadAndDecryptPassphrase`() = runTest {
        coEvery {
            mockLoadAndDecryptPassphrase(mockInitializedDecryptionCipher)
        } returns Err(PassphraseError.createGeneralError(Exception()))

        useCase(mockBiometricPromptWrapper).assertErrorType(LoginError.General::class)

        coVerify(exactly = 1) {
            mockResetBiometrics()
        }
    }

    @Test
    fun `LoginWithBiometrics returns an error if the DB setup failed`() = runTest {
        coEvery {
            mockOpenAppDatabase(mockDecryptedPassphrase)
        } returns Err(DatabaseError.createSetupFailedError(Exception()))

        useCase(mockBiometricPromptWrapper).assertErrorType(LoginError.General::class)

        coVerify(exactly = 1) {
            mockResetBiometrics()
        }
    }

    @Test
    fun `LoginWithBiometrics returns an error if the DB is already open`() = runTest {
        coEvery {
            mockOpenAppDatabase(mockDecryptedPassphrase)
        } returns Err(DatabaseError.createAlreadyOpenError())

        useCase(mockBiometricPromptWrapper).assertErrorType(LoginError.General::class)

        coVerify(exactly = 1) {
            mockResetBiometrics()
        }
    }

    private fun setupDefaultMocks() {
        coEvery { mockGetBiometricsCipher() } returns Ok(mockCipher)
        coEvery {
            mockLaunchBiometricPrompt(mockBiometricPromptWrapper, mockCipher)
        } returns Ok(mockInitializedDecryptionCipher)
        coEvery { mockLoadAndDecryptPassphrase(mockInitializedDecryptionCipher) } returns Ok(mockDecryptedPassphrase)
        coEvery { mockOpenAppDatabase(mockDecryptedPassphrase) } returns Ok(Unit)
        coEvery { mockUserInteraction() } just runs
        coEvery { mockResetBiometrics() } returns Ok(Unit)
    }
}
