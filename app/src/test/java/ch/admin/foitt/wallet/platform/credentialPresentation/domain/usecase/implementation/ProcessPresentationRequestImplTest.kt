package ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptor
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CredentialPresentationError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestWithRaw
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ProcessPresentationRequestResult
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.GetCompatibleCredentials
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ProcessPresentationRequest
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
    private lateinit var mockVerifiableCredentialRepo: VerifiableCredentialRepository

    @MockK
    private lateinit var mockGetCompatibleCredentials: GetCompatibleCredentials

    @MockK
    private lateinit var mockAuthorizationRequest: AuthorizationRequest

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
        coEvery { mockGetCompatibleCredentials(mockAuthorizationRequest) } returns Ok(setOf(mockCompatibleCredential))

        val result = useCase(mockPresentationRequestWithRaw)

        val expected = ProcessPresentationRequestResult.Credential(
            mockCompatibleCredential,
            mockPresentationRequestWithRaw,
        )
        val processPresentationResult = result.assertSuccessType(ProcessPresentationRequestResult.Credential::class)
        assertEquals(expected, processPresentationResult)
    }

    @Test
    fun `Processing presentation request which matches multiple credentials returns the credentials`() = runTest {
        val credentials = setOf(mockCompatibleCredential, mockCompatibleCredential2)
        coEvery { mockGetCompatibleCredentials(mockAuthorizationRequest) } returns Ok(credentials)

        val result = useCase(mockPresentationRequestWithRaw)

        val expected = ProcessPresentationRequestResult.CredentialList(
            credentials,
            mockPresentationRequestWithRaw,
        )
        val processPresentationResult = result.assertSuccessType(ProcessPresentationRequestResult.CredentialList::class)
        assertEquals(expected, processPresentationResult)
    }

    @Test
    fun `Processing presentation request which matches no credential returns no compatible credential error`() = runTest {
        coEvery { mockGetCompatibleCredentials(mockAuthorizationRequest) } returns Ok(emptySet())

        val result = useCase(mockPresentationRequestWithRaw)

        result.assertErrorType(CredentialPresentationError.NoCompatibleCredential::class)
    }

    @Test
    fun `Processing presentation request with empty wallet returns empty wallet error`() = runTest {
        coEvery { mockVerifiableCredentialRepo.getAllIds() } returns Ok(emptyList())

        val result = useCase(mockPresentationRequestWithRaw)

        result.assertErrorType(CredentialPresentationError.EmptyWallet::class)
    }

    @Test
    fun `Processing presentation request maps errors from getting all credentials`() = runTest {
        val exception = IllegalStateException()
        coEvery { mockVerifiableCredentialRepo.getAllIds() } returns Err(SsiError.Unexpected(exception))

        val result = useCase(mockPresentationRequestWithRaw)

        val error = result.assertErrorType(CredentialPresentationError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    @Test
    fun `Processing presentation request maps errors from getting compatible credentials`() = runTest {
        val exception = IllegalStateException()
        coEvery {
            mockGetCompatibleCredentials(mockAuthorizationRequest)
        } returns Err(CredentialPresentationError.Unexpected(exception))

        val result = useCase(mockPresentationRequestWithRaw)

        val error = result.assertErrorType(CredentialPresentationError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    private fun setupDefaultMocks() {
        every { mockAuthorizationRequest.presentationDefinition } returns mockk {
            every { inputDescriptors } returns mockInputDescriptors
        }
        every { mockAuthorizationRequest.responseUri } returns RESPONSE_URI
        coEvery { mockVerifiableCredentialRepo.getAllIds() } returns Ok(listOf(1, 2, 3))
        coEvery { mockGetCompatibleCredentials(mockAuthorizationRequest) } returns Ok(setOf(mockCompatibleCredential))
        coEvery { mockJwt.rawJwt } returns RAW_JWT

        every { mockPresentationRequestWithRaw.authorizationRequest } returns mockAuthorizationRequest
        every { mockPresentationRequestWithRaw.rawPresentationRequest } returns RAW_JWT
    }

    private companion object {
        const val RESPONSE_URI = "response uri"
        const val RAW_JWT = "rawJwt"
    }
}
