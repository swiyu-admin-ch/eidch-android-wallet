package ch.admin.foitt.wallet.feature.settings

import android.annotation.SuppressLint
import ch.admin.foitt.wallet.platform.crypto.domain.model.HashedData
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PepperedData
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.EncryptAndSavePassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.HashPassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.PepperPassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.StorePassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.implementation.StorePassphraseImpl
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.crypto.Cipher

class StorePassphraseTest {

    @MockK
    private lateinit var mockHashPassphrase: HashPassphrase

    @MockK
    private lateinit var mockPepperPassphrase: PepperPassphrase

    @MockK
    private lateinit var mockEncryptAndSavePassphrase: EncryptAndSavePassphrase

    @MockK
    private lateinit var mockCipher: Cipher

    private val hashedData = HashedData(byteArrayOf(0, 1), byteArrayOf(1, 0))
    private val pepperedData = PepperedData(byteArrayOf(0, 0), byteArrayOf(1, 1))

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var useCase: StorePassphrase

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = StorePassphraseImpl(
            hashPassphrase = mockHashPassphrase,
            encryptAndSavePassphrase = mockEncryptAndSavePassphrase,
            pepperPassphrase = mockPepperPassphrase,
            ioDispatcher = testDispatcher,
        )

        coEvery { mockHashPassphrase(any(), any()) } returns Ok(hashedData)
        coEvery { mockEncryptAndSavePassphrase(any(), any()) } returns Ok(Unit)
        coEvery { mockPepperPassphrase(any(), any()) } returns Ok(pepperedData)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @SuppressLint("CheckResult")
    @Test
    fun `Saving the passphrase should follow specific steps`() = runTest(testDispatcher) {
        useCase(passphrase = "123456", mockCipher).assertOk()

        coVerifyOrder {
            mockHashPassphrase(any(), any())
            mockPepperPassphrase(any(), any())
            mockEncryptAndSavePassphrase(any(), any())
        }
    }

    @SuppressLint("CheckResult")
    @Test
    fun `A failed hash should fail the passphrase storing`() = runTest(testDispatcher) {
        val exception = PassphraseError.createGeneralError(Exception())
        coEvery { mockHashPassphrase(any(), any()) } returns Err(exception)

        useCase(passphrase = "123456", mockCipher).assertErrorType(PassphraseError.General::class)

        coVerify(exactly = 0) {
            mockPepperPassphrase(any(), any())
        }
    }

    @SuppressLint("CheckResult")
    @Test
    fun `A failed pepper should fail the passphrase storing`() = runTest(testDispatcher) {
        val exception = PassphraseError.createGeneralError((Exception()))
        coEvery { mockPepperPassphrase(any(), any()) } returns Err(exception)

        useCase(passphrase = "123456", mockCipher).assertErrorType(PassphraseError.General::class)

        coVerify(exactly = 0) {
            mockEncryptAndSavePassphrase(any(), any())
        }
    }

    @SuppressLint("CheckResult")
    @Test
    fun `A failed passphrase encryption should return an error`() = runTest(testDispatcher) {
        val encryptError = PassphraseError.createGeneralError(Exception("EncryptionException"))

        coEvery { mockEncryptAndSavePassphrase(any(), any()) } returns Err(encryptError)

        useCase(passphrase = "pin", mockCipher).assertErrorType(PassphraseError.General::class)

        coVerifyOrder {
            mockHashPassphrase(any(), any())
            mockPepperPassphrase(any(), any())
            mockEncryptAndSavePassphrase(any(), any())
        }
    }
}
