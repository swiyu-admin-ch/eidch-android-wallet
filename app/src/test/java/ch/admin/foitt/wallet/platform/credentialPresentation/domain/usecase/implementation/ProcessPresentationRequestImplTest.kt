package ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptor
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestContainer
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CredentialPresentationError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestWithRaw
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ProcessPresentationRequestResult
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.GetCompatibleCredentials
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ProcessPresentationRequest
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ValidatePresentationRequest
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialRepository
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertSuccessType
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ProcessPresentationRequestImplTest {

    @MockK
    private lateinit var mockValidatePresentationRequest: ValidatePresentationRequest

    @MockK
    private lateinit var mockVerifiableCredentialRepo: VerifiableCredentialRepository

    @MockK
    private lateinit var mockGetCompatibleCredentials: GetCompatibleCredentials

    @MockK
    private lateinit var mockPresentationRequest: PresentationRequest

    @MockK
    private lateinit var mockJwtPresentationContainer: PresentationRequestContainer.Jwt

    @MockK
    private lateinit var mockJsonPresentationContainer: PresentationRequestContainer.Json

    @MockK
    private lateinit var mockJwt: Jwt

    @MockK
    private lateinit var mockPresentationRequestWithRaw: PresentationRequestWithRaw

    @MockK
    private lateinit var mockInputDescriptors: List<InputDescriptor>

    @MockK
    private lateinit var mockCompatibleCredential: CompatibleCredential

    @MockK
    private lateinit var mockCompatibleCredential2: CompatibleCredential

    private lateinit var useCase: ProcessPresentationRequest

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = ProcessPresentationRequestImpl(
            mockValidatePresentationRequest,
            mockGetCompatibleCredentials,
            mockVerifiableCredentialRepo
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Processing presentation request which matches one credential returns the credential`() = runTest {
        coEvery { mockGetCompatibleCredentials(mockInputDescriptors) } returns Ok(setOf(mockCompatibleCredential))

        val result = useCase(mockJwtPresentationContainer)

        val expected = ProcessPresentationRequestResult.Credential(
            mockCompatibleCredential,
            mockPresentationRequestWithRaw,
            true,
        )
        val processPresentationResult = result.assertSuccessType(ProcessPresentationRequestResult.Credential::class)
        assertEquals(expected, processPresentationResult)
    }

    @Test
    fun `Processing presentation request which matches multiple credentials returns the credentials`() = runTest {
        val credentials = setOf(mockCompatibleCredential, mockCompatibleCredential2)
        coEvery { mockGetCompatibleCredentials(mockInputDescriptors) } returns Ok(credentials)

        val result = useCase(mockJwtPresentationContainer)

        val expected = ProcessPresentationRequestResult.CredentialList(
            credentials,
            mockPresentationRequestWithRaw,
            true,
        )
        val processPresentationResult = result.assertSuccessType(ProcessPresentationRequestResult.CredentialList::class)
        assertEquals(expected, processPresentationResult)
    }

    @Test
    fun `Processing presentation request which matches no credential returns no compatible credential error`() = runTest {
        coEvery { mockGetCompatibleCredentials(mockInputDescriptors) } returns Ok(emptySet())

        val result = useCase(mockJwtPresentationContainer)

        result.assertErrorType(CredentialPresentationError.NoCompatibleCredential::class)
    }

    @Test
    fun `Processing presentation request with empty wallet returns empty wallet error`() = runTest {
        coEvery { mockVerifiableCredentialRepo.getAllIds() } returns Ok(emptyList())

        val result = useCase(mockJwtPresentationContainer)

        result.assertErrorType(CredentialPresentationError.EmptyWallet::class)
    }

    @Test
    fun `Processing presentation request maps errors from request validation`() = runTest {
        coEvery {
            mockValidatePresentationRequest(mockJwtPresentationContainer)
        } returns Err(CredentialPresentationError.InvalidPresentation(RESPONSE_URI))

        useCase(mockJwtPresentationContainer).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
    }

    @Test
    fun `Processing presentation request maps errors from getting all credentials`() = runTest {
        val exception = IllegalStateException()
        coEvery { mockVerifiableCredentialRepo.getAllIds() } returns Err(SsiError.Unexpected(exception))

        val result = useCase(mockJwtPresentationContainer)

        val error = result.assertErrorType(CredentialPresentationError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    @Test
    fun `Processing presentation request maps errors from getting compatible credentials`() = runTest {
        val exception = IllegalStateException()
        coEvery {
            mockGetCompatibleCredentials(mockInputDescriptors)
        } returns Err(CredentialPresentationError.Unexpected(exception))

        val result = useCase(mockJwtPresentationContainer)

        val error = result.assertErrorType(CredentialPresentationError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    @Test
    fun `Processing a presentation wrapped in a JWT should return a 'true' for the TrustStatement flag`(): Unit = runTest {
        val result = useCase(mockJwtPresentationContainer)
        val processPresentationResult = result.assertSuccessType(ProcessPresentationRequestResult.Credential::class)

        assert(processPresentationResult.shouldFetchTrustStatements)
    }

    @Test
    fun `Processing a presentation wrapped in a JSON should return a 'false' for the TrustStatement flag`(): Unit = runTest {
        val result = useCase(mockJsonPresentationContainer)
        val processPresentationResult = result.assertSuccessType(ProcessPresentationRequestResult.Credential::class)

        assert(!processPresentationResult.shouldFetchTrustStatements)
    }

    private fun setupDefaultMocks() {
        every { mockPresentationRequest.presentationDefinition } returns mockk {
            every { inputDescriptors } returns mockInputDescriptors
        }
        every { mockPresentationRequest.responseUri } returns "uri"
        coEvery { mockValidatePresentationRequest(any()) } returns Ok(mockPresentationRequestWithRaw)
        coEvery { mockVerifiableCredentialRepo.getAllIds() } returns Ok(listOf(1, 2, 3))
        coEvery { mockGetCompatibleCredentials(mockInputDescriptors) } returns Ok(setOf(mockCompatibleCredential))
        coEvery { mockJwtPresentationContainer.jwt } returns mockJwt
        coEvery { mockJwt.rawJwt } returns RAW_JWT

        every { mockPresentationRequestWithRaw.presentationRequest } returns mockPresentationRequest
        every { mockPresentationRequestWithRaw.rawPresentationRequest } returns RAW_JWT
    }

    private companion object {
        const val RESPONSE_URI = "response uri"
        const val RAW_JWT = "rawJwt"
    }
}
