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
import com.github.michaelbull.result.annotation.UnsafeResultValueAccess
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetDeferredCredentialsWithDetailsFlowImplTest {

    @MockK
    private lateinit var mockAppContext: Context

    @MockK
    private lateinit var mockGetLocalizedAndThemedDisplay: GetLocalizedAndThemedDisplay

    @MockK
    private lateinit var mockDeferredCredentialWithDisplaysRepository: DeferredCredentialWithDisplaysRepository

    private val mockCredentialList = listOf(deferredCredentialWithDisplays)
    private val mockCredentialFlow = MutableStateFlow(Ok(mockCredentialList))

    private lateinit var useCase: GetDeferredCredentialsWithDetailsFlowImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = GetDeferredCredentialsWithDetailsFlowImpl(
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
        val result = useCase()

        val credentialList = result.first().assertOk()
        assertEquals(1, credentialList.size)

        coVerifyOrder {
            mockDeferredCredentialWithDisplaysRepository.getAllFlow()
            mockGetLocalizedAndThemedDisplay(any(), any())
        }
    }

    @OptIn(UnsafeResultValueAccess::class)
    @Test
    fun `A successful call properly handle a multiple items list`() = runTest {
        val list = listOf(
            deferredCredentialWithDisplays,
            deferredCredentialWithDisplays2,
        )
        mockCredentialFlow.value = Ok(list)
        val useCaseFlow = useCase()

        val credentialList1 = useCaseFlow.first().assertOk()
        assertEquals(2, credentialList1.size)
        assertEquals(CREDENTIAL_ID, credentialList1[0].credentialId)
        assertEquals(CREDENTIAL_ID2, credentialList1[1].credentialId)

        mockCredentialFlow.update { Ok(it.value + deferredCredentialWithDisplays) }
        val credentialList2 = useCaseFlow.first().assertOk()
        assertEquals(CREDENTIAL_ID, credentialList2[2].credentialId)
    }

    @Test
    fun `Errors returned by the DeferredCredentialWithDisplay repository are propagated`() = runTest {
        val exception = Exception("DB error")
        coEvery {
            mockDeferredCredentialWithDisplaysRepository.getAllFlow()
        } returns flowOf(Err(SsiError.Unexpected(exception)))

        val result = useCase().first()
        val error = result.assertErrorType(SsiError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    @Test
    fun `Null result returned by the GetLocalizedDisplay usecase are propagated`() = runTest {
        coEvery { mockGetLocalizedAndThemedDisplay(any(), any()) } returns null
        val result = useCase().first()
        result.assertErrorType(GetCredentialsWithDetailsFlowError::class)
    }

    private fun setupDefaultMocks() {
        coEvery { mockDeferredCredentialWithDisplaysRepository.getAllFlow() } returns mockCredentialFlow
        coEvery { mockGetLocalizedAndThemedDisplay(any(), any()) } returns credentialDisplay
    }

    companion object {
        const val CREDENTIAL_ID = 1L
        const val CREDENTIAL_ID2 = 13L
        const val ENDPOINT = "endpoint"
        const val ENDPOINT2 = "endpoint2"
        const val NAME = "name"
        const val LOGO_URI = "logoUri"
        const val BACKGROUND_COLOR = "backgroundColor"
        const val LOCALE = "locale"

        val credentialDisplay = createCredentialDisplay()
        val credentialDisplay2 = credentialDisplay.copy(
            credentialId = CREDENTIAL_ID2,
        )

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
            endpoint = ENDPOINT,
        )

        private val deferredCredential2 = deferredCredential.copy(
            credentialId = CREDENTIAL_ID2,
            endpoint = ENDPOINT2,
        )

        private val deferredCredentialWithDisplays = DeferredCredentialWithDisplays(
            deferredCredential = deferredCredential,
            credentialDisplays = listOf(credentialDisplay),
        )

        private val deferredCredentialWithDisplays2 = DeferredCredentialWithDisplays(
            deferredCredential = deferredCredential2,
            credentialDisplays = listOf(credentialDisplay2),
        )
    }
}
