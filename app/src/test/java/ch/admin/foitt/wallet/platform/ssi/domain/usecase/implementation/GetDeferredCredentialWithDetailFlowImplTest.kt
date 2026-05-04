package ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation

import android.content.Context
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialEntity
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialWithDisplays
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedAndThemedDisplay
import ch.admin.foitt.wallet.platform.ssi.domain.model.GetCredentialsWithDetailsFlowError
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.DeferredCredentialWithDisplaysRepository
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetDeferredCredentialWithDetailFlowImplTest {

    @MockK
    private lateinit var mockAppContext: Context

    @MockK
    private lateinit var mockGetLocalizedAndThemedDisplay: GetLocalizedAndThemedDisplay

    @MockK
    private lateinit var mockDeferredCredentialWithDisplaysRepository: DeferredCredentialWithDisplaysRepository

    private val mockCredentialFlow = MutableStateFlow(Ok(deferredCredentialWithDisplays))

    private lateinit var useCase: GetDeferredCredentialWithDetailFlowImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = GetDeferredCredentialWithDetailFlowImpl(
            context = mockAppContext,
            deferredCredentialWithDisplaysRepository = mockDeferredCredentialWithDisplaysRepository,
            getLocalizedAndThemedDisplay = mockGetLocalizedAndThemedDisplay,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `A successful call returns the flow and follow specific steps`() = runTest {
        val result = useCase(CREDENTIAL_ID).firstOrNull()

        assertNotNull(result)
        result?.assertOk()

        coVerifyOrder {
            mockDeferredCredentialWithDisplaysRepository.getByIdFlow(any())
            mockGetLocalizedAndThemedDisplay(any(), any())
        }
    }

    @Test
    fun `Errors returned by the DeferredCredentialWithDisplay repository are propagated`() = runTest {
        val exception = Exception("DB error")
        coEvery {
            mockDeferredCredentialWithDisplaysRepository.getByIdFlow(any())
        } returns flowOf(Err(SsiError.Unexpected(exception)))

        val result = useCase(CREDENTIAL_ID).first()
        val error = result.assertErrorType(SsiError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    @Test
    fun `Null result returned by the GetLocalizedDisplay usecase are propagated`() = runTest {
        coEvery { mockGetLocalizedAndThemedDisplay(any(), any()) } returns null
        val result = useCase(CREDENTIAL_ID).first()
        result.assertErrorType(GetCredentialsWithDetailsFlowError::class)
    }

    private fun setupDefaultMocks() {
        coEvery { mockDeferredCredentialWithDisplaysRepository.getByIdFlow(any()) } returns mockCredentialFlow
        coEvery { mockGetLocalizedAndThemedDisplay(any(), any()) } returns credentialDisplay
    }

    companion object {
        const val CREDENTIAL_ID = 1L
        const val ENDPOINT = "endpoint"
        const val NAME = "name"
        const val LOGO_URI = "logoUri"
        const val BACKGROUND_COLOR = "backgroundColor"
        const val LOCALE = "locale"

        val credentialDisplay = createCredentialDisplay()

        private fun createCredentialDisplay() = CredentialDisplay(
            id = 1,
            credentialId = CREDENTIAL_ID,
            locale = LOCALE,
            name = NAME,
            logoUri = LOGO_URI,
            backgroundColor = BACKGROUND_COLOR,
        )

        private val deferredCredential = DeferredCredentialEntity(
            credentialId = CREDENTIAL_ID,
            transactionId = "transactionId",
            accessToken = "accessToken",
            refreshToken = "refreshToken",
            endpoint = ENDPOINT,
        )

        private val deferredCredentialWithDisplays = DeferredCredentialWithDisplays(
            deferredCredential = deferredCredential,
            credentialDisplays = listOf(credentialDisplay),
        )
    }
}
