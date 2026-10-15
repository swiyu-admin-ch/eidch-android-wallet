package ch.admin.foitt.wallet.platform.login.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.biometricPrompt.domain.model.BiometricManagerResult
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.usecase.BiometricsStatus
import ch.admin.foitt.wallet.platform.biometrics.domain.model.BiometricsError
import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.GetBiometricsCipher
import ch.admin.foitt.wallet.platform.login.domain.model.CanUseBiometricsForLoginResult
import ch.admin.foitt.wallet.platform.login.domain.usecase.BiometricLoginEnabled
import ch.admin.foitt.wallet.platform.login.domain.usecase.CanUseBiometricsForLogin
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.crypto.Cipher

class CanUseBiometricsForLoginImplTest {
    private val testDispatcher = StandardTestDispatcher()

    @MockK
    private lateinit var mockBiometricsStatus: BiometricsStatus

    @MockK
    private lateinit var mockBiometricLoginEnabled: BiometricLoginEnabled

    @MockK
    private lateinit var mockGetBiometricsCipher: GetBiometricsCipher

    @MockK
    private lateinit var mockCipher: Cipher

    private lateinit var useCase: CanUseBiometricsForLogin

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { mockBiometricsStatus() } returns BiometricManagerResult.Available
        coEvery { mockBiometricLoginEnabled() } returns true
        coEvery { mockGetBiometricsCipher() } returns Ok(mockCipher)

        useCase = CanUseBiometricsForLoginImpl(
            ioDispatcher = testDispatcher,
            biometricsStatus = mockBiometricsStatus,
            biometricLoginEnabled = mockBiometricLoginEnabled,
            getBiometricsCipher = mockGetBiometricsCipher,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Calling CanUseBiometricsForLogin when biometrics are not available for login returns NotSetUpInApp`() = runTest(testDispatcher) {
        coEvery { mockBiometricLoginEnabled() } returns false

        assertEquals(CanUseBiometricsForLoginResult.NotSetUpInApp, useCase())
        coVerify(exactly = 1) {
            mockBiometricLoginEnabled()
        }

        coVerify(exactly = 0) {
            mockBiometricsStatus()
            mockGetBiometricsCipher()
        }
    }

    @Test
    fun `Calling CanUseBiometricsForLogin when the biometrics cypher is not usable returns Changed`() = runTest(testDispatcher) {
        val error = BiometricsError.createGeneralError()
        coEvery { mockGetBiometricsCipher() } returns Err(error)

        assertEquals(CanUseBiometricsForLoginResult.Changed, useCase())

        coVerify(exactly = 1) {
            mockBiometricLoginEnabled()
            mockGetBiometricsCipher()
        }

        coVerify(exactly = 0) {
            mockBiometricsStatus()
        }
    }

    @Test
    fun `Calling CanUseBiometricsForLogin when the biometrics are available returns Usable`() = runTest(testDispatcher) {
        assertEquals(CanUseBiometricsForLoginResult.Usable, useCase())

        coVerify(exactly = 1) {
            mockBiometricLoginEnabled()
            mockGetBiometricsCipher()
            mockBiometricsStatus()
        }
    }

    @Test
    fun `Calling CanUseBiometricsForLogin when the biometrics can be enrolled on the device returns RemovedInDeviceSettings`() =
        runTest(testDispatcher) {
            coEvery { mockBiometricsStatus() } returns BiometricManagerResult.CanEnroll

            assertEquals(CanUseBiometricsForLoginResult.RemovedInDeviceSettings, useCase())
        }

    @Test
    fun `Calling CanUseBiometricsForLogin when the biometrics are disabled in the device settings returns DeactivatedInDeviceSettings`() =
        runTest(testDispatcher) {
            coEvery { mockBiometricsStatus() } returns BiometricManagerResult.Disabled

            assertEquals(CanUseBiometricsForLoginResult.DeactivatedInDeviceSettings, useCase())
        }

    @Test
    fun `Calling CanUseBiometricsForLogin when biometrics are unsupported on the device returns NoHardwareAvailable`() =
        runTest(testDispatcher) {
            coEvery { mockBiometricsStatus() } returns BiometricManagerResult.Unsupported

            assertEquals(CanUseBiometricsForLoginResult.NoHardwareAvailable, useCase())
        }
}
