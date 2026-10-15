package ch.admin.foitt.wallet.feature.onboarding

import android.annotation.SuppressLint
import ch.admin.foitt.wallet.platform.crypto.domain.model.HashedData
import ch.admin.foitt.wallet.platform.database.domain.model.DatabaseError
import ch.admin.foitt.wallet.platform.database.domain.usecase.CreateAppDatabase
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PepperedData
import ch.admin.foitt.wallet.platform.passphrase.domain.repository.PepperIvRepository
import ch.admin.foitt.wallet.platform.passphrase.domain.repository.SaltRepository
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.EncryptAndSavePassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.HashPassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.InitializePassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.PepperPassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.implementation.InitializePassphraseImpl
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.unmockkAll
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.crypto.Cipher

internal class InitializePassphraseTest {

    @MockK
    private lateinit var mockHashPassphrase: HashPassphrase

    @MockK
    private lateinit var mockPepperPassphrase: PepperPassphrase

    @MockK
    private lateinit var mockEncryptAndSavePassphrase: EncryptAndSavePassphrase

    @MockK
    private lateinit var mockCipher: Cipher

    @MockK
    private lateinit var mockCreateAppDatabase: CreateAppDatabase

    @MockK
    private lateinit var mockSaltRepository: SaltRepository

    @MockK
    private lateinit var mockPepperIvRepository: PepperIvRepository

    private val hashedData = HashedData(byteArrayOf(0, 1), byteArrayOf(1, 0))
    private val pepperedData = PepperedData(byteArrayOf(0, 0), byteArrayOf(1, 1))

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var useCase: InitializePassphrase

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        useCase = InitializePassphraseImpl(
            ioDispatcher = testDispatcher,
            encryptAndSavePassphrase = mockEncryptAndSavePassphrase,
            createAppDatabase = mockCreateAppDatabase,
            hashPassphrase = mockHashPassphrase,
            pepperPassphrase = mockPepperPassphrase,
            saltRepository = mockSaltRepository,
            pepperIvRepository = mockPepperIvRepository,
        )

        coEvery { mockEncryptAndSavePassphrase(any(), any()) } returns Ok(Unit)
        coEvery { mockCreateAppDatabase(any()) } returns Ok(Unit)
        coEvery { mockHashPassphrase(any(), any()) } returns Ok(hashedData)
        coEvery { mockPepperPassphrase(any(), any()) } returns Ok(pepperedData)
        coEvery { mockSaltRepository.save(any()) } just Runs
        coEvery { mockPepperIvRepository.save(any()) } just Runs
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @SuppressLint("CheckResult")
    @Test
    fun `A successful init without cipher should follow specific steps`() = runTest(testDispatcher) {
        useCase(passphrase = "0", null).assertOk()

        coVerify(exactly = 0) {
            mockEncryptAndSavePassphrase(any(), any())
        }

        coVerifyOrder {
            mockHashPassphrase(any(), any())
            mockPepperPassphrase(any(), any())
            mockCreateAppDatabase(any())
            mockSaltRepository.save(any())
            mockPepperIvRepository.save(any())
        }
    }

    @SuppressLint("CheckResult")
    @Test
    fun `A successful init with cipher should follow specific steps`() = runTest(testDispatcher) {
        useCase(passphrase = "0", mockCipher).assertOk()

        coVerifyOrder {
            mockHashPassphrase(any(), any())
            mockPepperPassphrase(any(), any())
            mockCreateAppDatabase(any())
            mockSaltRepository.save(any())
            mockPepperIvRepository.save(any())
            mockEncryptAndSavePassphrase(any(), any())
        }
    }

    @SuppressLint("CheckResult")
    @Test
    fun `A successful init should save the results in repositories`() = runTest(testDispatcher) {
        useCase(passphrase = "0", null)

        coVerifyOrder {
            mockSaltRepository.save(hashedData.salt)
            mockPepperIvRepository.save(pepperedData.initializationVector)
        }
    }

    @SuppressLint("CheckResult")
    @Test
    fun `A failed hash should fail the passphrase init`() = runTest(testDispatcher) {
        val error = PassphraseError.createGeneralError(Exception())
        coEvery { mockHashPassphrase(any(), any()) } returns Err(error)

        useCase(passphrase = "0", null).assertErrorType(PassphraseError.General::class)

        coVerify(exactly = 0) {
            mockPepperPassphrase(any(), any())
            mockCreateAppDatabase(any())
            mockSaltRepository.save(any())
            mockPepperIvRepository.save(any())
            mockEncryptAndSavePassphrase(any(), any())
        }
    }

    @SuppressLint("CheckResult")
    @Test
    fun `Failed peppering should fail the passphrase init`() = runTest(testDispatcher) {
        val error = PassphraseError.createGeneralError(Exception())
        coEvery { mockPepperPassphrase(any(), any()) } returns Err(error)

        useCase(passphrase = "0", null).assertErrorType(PassphraseError.General::class)

        coVerify(exactly = 0) {
            mockCreateAppDatabase(any())
            mockSaltRepository.save(any())
            mockPepperIvRepository.save(any())
            mockEncryptAndSavePassphrase(any(), any())
        }
    }

    @Test
    fun `Failed db creation should fail the passphrase init`() = runTest(testDispatcher) {
        val error = DatabaseError.createSetupFailedError(Exception())
        coEvery { mockCreateAppDatabase(any()) } returns Err(error)

        useCase(passphrase = "0", null).assertErrorType(PassphraseError.DatabaseSetupFailed::class)

        coVerify(exactly = 0) {
            mockSaltRepository.save(any())
            mockPepperIvRepository.save(any())
            mockEncryptAndSavePassphrase(any(), any())
        }
    }

    @Test
    fun `A failed passphrase encryption should return an error`() = runTest(testDispatcher) {
        val encryptError = PassphraseError.createGeneralError(Exception("EncryptionException"))

        coEvery { mockEncryptAndSavePassphrase(any(), any()) } returns Err(encryptError)

        useCase(passphrase = "pin", mockCipher).assertErrorType(PassphraseError.General::class)

        coVerify {
            mockEncryptAndSavePassphrase(any(), any())
        }
    }
}
