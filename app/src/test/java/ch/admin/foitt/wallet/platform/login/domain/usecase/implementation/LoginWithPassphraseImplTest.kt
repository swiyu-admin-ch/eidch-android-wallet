package ch.admin.foitt.wallet.platform.login.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.crypto.domain.model.HashedData
import ch.admin.foitt.wallet.platform.database.domain.model.DatabaseError
import ch.admin.foitt.wallet.platform.database.domain.usecase.OpenAppDatabase
import ch.admin.foitt.wallet.platform.login.domain.model.LoginError
import ch.admin.foitt.wallet.platform.login.domain.usecase.LoginWithPassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PepperedData
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.HashPassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.PepperPassphrase
import ch.admin.foitt.wallet.platform.userInteraction.domain.usecase.UserInteraction
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LoginWithPassphraseImplTest {
    @MockK
    private lateinit var mockHashPassphrase: HashPassphrase

    @MockK
    private lateinit var mockOpenAppDatabase: OpenAppDatabase

    @MockK
    private lateinit var mockPepperPassphrase: PepperPassphrase

    @MockK
    private lateinit var mockUserInteraction: UserInteraction

    @MockK
    private lateinit var mockHashedData: HashedData

    @MockK
    private lateinit var mockPepperedData: PepperedData

    private lateinit var useCase: LoginWithPassphrase

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = LoginWithPassphraseImpl(
            hashPassphrase = mockHashPassphrase,
            openAppDatabase = mockOpenAppDatabase,
            pepperPassphrase = mockPepperPassphrase,
            userInteraction = mockUserInteraction,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Logging in successfully with passphrase returns an Ok`() = runTest {
        useCase(PASSPHRASE).assertOk()
    }

    @Test
    fun `LoginWithPassphrase maps errors from HashPassphrase`() = runTest {
        coEvery { mockHashPassphrase(PASSPHRASE, false) } returns Err(PassphraseError.createGeneralError(Exception()))

        useCase(PASSPHRASE).assertErrorType(LoginError.General::class)
    }

    @Test
    fun `LoginWithPassphrase maps errors from PepperPassphrase`() = runTest {
        coEvery { mockPepperPassphrase(hashedDataHash, false) } returns Err(PassphraseError.createGeneralError(Exception()))

        useCase(PASSPHRASE).assertErrorType(LoginError.General::class)
    }

    @Test
    fun `LoginWithPassphrase returns an error if the provided DB passphrase is wrong`() = runTest {
        coEvery { mockOpenAppDatabase(pepperedDataHash) } returns Err(DatabaseError.createWrongPassphraseError(Exception()))

        useCase(PASSPHRASE).assertErrorType(LoginError.InvalidPassphrase::class)
    }

    @Test
    fun `LoginWithPassphrase returns an error if the DB is already open`() = runTest {
        coEvery { mockOpenAppDatabase(pepperedDataHash) } returns Err(DatabaseError.createAlreadyOpenError())

        useCase(PASSPHRASE).assertErrorType(LoginError.General::class)
    }

    @Test
    fun `LoginWithPassphrase returns an error if the DB setup failed`() = runTest {
        coEvery { mockOpenAppDatabase(pepperedDataHash) } returns Err(DatabaseError.createSetupFailedError(Exception()))

        useCase(PASSPHRASE).assertErrorType(LoginError.General::class)
    }

    private fun setupDefaultMocks() {
        every { mockHashedData.hash } returns hashedDataHash
        every { mockPepperedData.hash } returns pepperedDataHash

        coEvery { mockHashPassphrase(PASSPHRASE, false) } returns Ok(mockHashedData)
        coEvery { mockPepperPassphrase(hashedDataHash, false) } returns Ok(mockPepperedData)
        coEvery { mockOpenAppDatabase(pepperedDataHash) } returns Ok(Unit)
        coEvery { mockUserInteraction() } just runs
    }

    private companion object {
        const val PASSPHRASE = "mockPassphrase"
        val hashedDataHash = byteArrayOf()
        val pepperedDataHash = byteArrayOf()
    }
}
