package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchAndUpdateDeferredCredential
import ch.admin.foitt.wallet.platform.credential.domain.usecase.RefreshDeferredCredentials
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialEntity
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialWithKeyBinding
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredProgressionState
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.DeferredCredentialRepository
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL
import java.time.Instant

class RefreshDeferredCredentialsImplTest {

    @MockK
    private lateinit var mockDeferredCredentialRepository: DeferredCredentialRepository

    @MockK
    private lateinit var mockFetchAndUpdateDeferredCredential: FetchAndUpdateDeferredCredential

    private lateinit var useCase: RefreshDeferredCredentials

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = RefreshDeferredCredentialsImpl(
            deferredCredentialRepository = mockDeferredCredentialRepository,
            fetchAndUpdateDeferredCredential = mockFetchAndUpdateDeferredCredential,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    private fun setupDefaultMocks() {
        mockkStatic(Instant::class)
        coEvery { Instant.now().epochSecond } returns currentTime

        coEvery { mockDeferredCredentialRepository.getAll() } returns Ok(
            listOf(
                deferredCredentialEntityWithBinding01,
            )
        )

        coEvery {
            mockFetchAndUpdateDeferredCredential(deferredCredentialEntity = any())
        } returns Ok(Unit)
    }

    @Test
    fun `A simple refresh of the deferred credentials runs specific things`() = runTest {
        useCase().assertOk()

        coVerifyOrder {
            mockDeferredCredentialRepository.getAll()
            mockFetchAndUpdateDeferredCredential(deferredCredentialEntityWithBinding01)
        }
    }

    @Test
    fun `Deferred credentials are not refreshed if their interval has not passed`() = runTest {
        coEvery { Instant.now().epochSecond } returns polledAt01 + pollInterval01 - 1

        useCase().assertOk()

        coVerifyOrder {
            mockDeferredCredentialRepository.getAll()
        }

        coVerify(exactly = 0) {
            mockFetchAndUpdateDeferredCredential(deferredCredentialEntity = any())
        }
    }

    @Test
    fun `Deferred credentials are not refreshed if they are not in progress`() = runTest {
        coEvery { mockDeferredCredentialRepository.getAll() } returns Ok(
            listOf(
                DeferredCredentialWithKeyBinding(
                    deferredCredential = deferredCredentialEntity01.copy(progressionState = DeferredProgressionState.INVALID),
                    credential = credentialEntity01,
                    keyBindings = listOf(),
                )
            )
        )

        useCase().assertOk()

        coVerifyOrder {
            mockDeferredCredentialRepository.getAll()
        }

        coVerify(exactly = 0) {
            mockFetchAndUpdateDeferredCredential(any())
        }
    }

    @Test
    fun `A deferred credential repository error is mapped`() = runTest {
        val exception = Exception("my exception")
        coEvery { mockDeferredCredentialRepository.getAll() } returns Err(SsiError.Unexpected(exception))

        val error = useCase().assertErrorType(CredentialError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    @Test
    fun `A fetchAndUpdate error is ignored`() = runTest {
        coEvery { mockFetchAndUpdateDeferredCredential(any()) } returns Err(CredentialError.NetworkError)
        useCase().assertOk()
    }

    @Suppress("MayBeConst")
    private companion object {
        private val currentTime = Instant.ofEpochSecond(15L).epochSecond
        private val transactionId01 = "transactionId01"
        private val accessToken01 = "accessToken01"
        private val refreshToken01 = "refreshToken01"
        private val issuerEndpoint01 = "https://example"
        private val polledAt01 = Instant.ofEpochSecond(5L).epochSecond
        private val pollInterval01 = 10
        private val selectedConfigurationId01 = "selectedConfigurationId01"
        private const val ISSUER01_URL = "https://example.com/issuer"
        private val deferredCredentialEntity01 = DeferredCredentialEntity(
            credentialId = 1L,
            progressionState = DeferredProgressionState.IN_PROGRESS,
            transactionId = transactionId01,
            accessToken = accessToken01,
            refreshToken = refreshToken01,
            endpoint = issuerEndpoint01,
            pollInterval = pollInterval01,
            createdAt = Instant.ofEpochSecond(4L).epochSecond,
            polledAt = polledAt01,
        )

        private val credentialEntity01 = Credential(
            id = 1L,
            format = CredentialFormat.VC_SD_JWT,
            createdAt = Instant.ofEpochSecond(1L).epochSecond,
            selectedConfigurationId = selectedConfigurationId01,
            issuerUrl = URL(ISSUER01_URL)
        )

        private val deferredCredentialEntityWithBinding01 = DeferredCredentialWithKeyBinding(
            deferredCredential = deferredCredentialEntity01,
            credential = credentialEntity01,
            keyBindings = listOf(),
        )
    }
}
