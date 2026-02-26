package ch.admin.foitt.wallet.feature.presentationRequest

import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.PresentationCredentialDisplayData
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.PresentationRequestError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.GetPresentationRequestCredentialListFlow
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.implementation.GetPresentationRequestCredentialListFlowImpl
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialDisplayData
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetCredentialsWithDetailsFlow
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getError
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetPresentationRequestCredentialListFlowImplTest {

    @MockK
    lateinit var mockGetCredentialsWithDetailsFlow: GetCredentialsWithDetailsFlow

    @MockK
    lateinit var mockCredentialDisplayData1: CredentialDisplayData

    @MockK
    lateinit var mockCredentialDisplayData2: CredentialDisplayData

    @MockK
    lateinit var mockCompatibleCredential: CompatibleCredential

    private lateinit var getPresentationRequestCredentialListFlow: GetPresentationRequestCredentialListFlow

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        getPresentationRequestCredentialListFlow = GetPresentationRequestCredentialListFlowImpl(
            mockGetCredentialsWithDetailsFlow,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Getting the presentation request credential list flow returns a flow with one credential preview`() = runTest {
        val result = getPresentationRequestCredentialListFlow(
            compatibleCredentials = setOf(mockCompatibleCredential),
        ).firstOrNull()

        assertNotNull(result)
        val displayData: PresentationCredentialDisplayData? = result?.assertOk()
        val credentialPreviews = displayData?.credentials
        assertEquals(1, credentialPreviews?.size)
        assertEquals(COMPATIBLE_CREDENTIAL_ID, credentialPreviews?.first()?.credentialId)
    }

    @Test
    fun `Getting the presentation request credential list flow maps errors from GetCredentialsWithDetailsFlow`() = runTest {
        val exception = IllegalStateException("db error")
        coEvery {
            mockGetCredentialsWithDetailsFlow()
        } returns flowOf(Err(SsiError.Unexpected(exception)))

        val result = getPresentationRequestCredentialListFlow(
            compatibleCredentials = setOf(mockCompatibleCredential),
        ).firstOrNull()

        assertNotNull(result)
        result?.assertErrorType(PresentationRequestError.Unexpected::class)
        val error = result?.getError() as PresentationRequestError.Unexpected
        assertEquals(exception, error.throwable)
    }

    private fun setupDefaultMocks() {
        coEvery {
            mockGetCredentialsWithDetailsFlow()
        } returns flowOf(Ok(listOf(mockCredentialDisplayData1, mockCredentialDisplayData2)))

        every { mockCredentialDisplayData1.credentialId } returns CREDENTIAL_ID1
        every { mockCredentialDisplayData2.credentialId } returns CREDENTIAL_ID2

        coEvery { mockCompatibleCredential.credentialId } returns COMPATIBLE_CREDENTIAL_ID
    }

    private companion object {
        const val CREDENTIAL_ID1 = 1L
        const val CREDENTIAL_ID2 = 2L
        const val COMPATIBLE_CREDENTIAL_ID = 2L
    }
}
