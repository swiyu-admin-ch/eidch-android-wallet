package ch.admin.foitt.wallet.platform.invitation.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOffer
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchAndSaveCredential
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CredentialPresentationError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestWithRaw
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ProcessPresentationRequestResult
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ProcessPresentationRequest
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.model.ProcessInvitationResult
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.ProcessInvitation
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.ValidateInvitation
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import ch.admin.foitt.wallet.util.assertSuccessType
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ProcessInvitationImplTest {

    @MockK
    private lateinit var mockValidateInvitation: ValidateInvitation

    @MockK
    private lateinit var mockFetchAndSaveCredential: FetchAndSaveCredential

    @MockK
    private lateinit var mockProcessPresentationRequest: ProcessPresentationRequest

    @MockK
    private lateinit var mockPresentationRequestWithRaw: PresentationRequestWithRaw

    @MockK
    private lateinit var mockCredentialOffer: CredentialOffer

    private lateinit var useCase: ProcessInvitation

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        setupDefaultMocks()

        useCase = ProcessInvitationImpl(
            validateInvitation = mockValidateInvitation,
            fetchAndSaveCredential = mockFetchAndSaveCredential,
            processPresentationRequest = mockProcessPresentationRequest,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Processing a valid credential offer returns a credential id`() = runTest {
        val result = useCase(INVITATION_URI)

        val expected = ProcessInvitationResult.CredentialOffer(CREDENTIAL_ID)
        assertEquals(expected, result.assertOk())
    }

    @Test
    fun `Processing a valid credential maps errors from fetching credential`() = runTest {
        val exception = IllegalStateException()
        coEvery { mockValidateInvitation(INVITATION_URI) } returns Ok(mockCredentialOffer)
        coEvery { mockFetchAndSaveCredential(mockCredentialOffer) } returns Err(CredentialError.Unexpected(exception))

        val result = useCase(INVITATION_URI)

        result.assertErrorType(InvitationError.Unexpected::class)
    }

    @Test
    fun `Processing a valid presentation request matching one credential returns the credential and request`() = runTest {
        val request = mockk<PresentationRequestWithRaw>()
        val credential = mockk<CompatibleCredential>()
        val requestResult = ProcessPresentationRequestResult.Credential(credential, request)
        coEvery { mockValidateInvitation(INVITATION_URI) } returns Ok(mockPresentationRequestWithRaw)
        coEvery { mockProcessPresentationRequest(mockPresentationRequestWithRaw) } returns Ok(requestResult)

        val result = useCase(INVITATION_URI)

        val processInvitationResult = result.assertSuccessType(ProcessInvitationResult.PresentationRequest::class)
        val expected = ProcessInvitationResult.PresentationRequest(credential, request)
        assertEquals(expected, processInvitationResult)
    }

    @Test
    fun `Processing a valid presentation request matching multiple credential returns the credentials and request`() = runTest {
        val request = mockk<PresentationRequestWithRaw>()
        val credentials = setOf(mockk<CompatibleCredential>())
        val requestResult = ProcessPresentationRequestResult.CredentialList(credentials, request)
        coEvery { mockValidateInvitation(INVITATION_URI) } returns Ok(mockPresentationRequestWithRaw)
        coEvery { mockProcessPresentationRequest(mockPresentationRequestWithRaw) } returns Ok(requestResult)

        val result = useCase(INVITATION_URI)

        val processInvitationResult = result.assertSuccessType(ProcessInvitationResult.PresentationRequestCredentialList::class)
        val expected = ProcessInvitationResult.PresentationRequestCredentialList(credentials, request)
        assertEquals(expected, processInvitationResult)
    }

    @Test
    fun `Processing a valid presentation request maps errors from processing presentation request`() = runTest {
        val exception = IllegalStateException()
        coEvery { mockValidateInvitation(INVITATION_URI) } returns Ok(mockPresentationRequestWithRaw)
        coEvery {
            mockProcessPresentationRequest(mockPresentationRequestWithRaw)
        } returns Err(CredentialPresentationError.Unexpected(exception))

        val result = useCase(INVITATION_URI)

        result.assertErrorType(InvitationError.Unexpected::class)

        coVerify(exactly = 1) {
            mockProcessPresentationRequest.invoke(any())
        }
    }

    @Test
    fun `Processing an invalid invitation maps errors from validating invitation`() = runTest {
        coEvery { mockValidateInvitation(INVITATION_URI) } returns Err(InvitationError.Unexpected)

        val result = useCase(INVITATION_URI)

        result.assertErrorType(InvitationError.Unexpected::class)
    }

    @Test
    fun `Processing an unsupported invitation type returns an error`() = runTest {
        coEvery { mockValidateInvitation(INVITATION_URI) } returns Ok(mockk())

        val result = useCase(INVITATION_URI)

        result.assertErrorType(InvitationError.Unexpected::class)
    }

    @Test
    fun `Processing a valid deferred credential succeeds`() = runTest {
        val deferredCredential = FetchCredentialResult.DeferredCredential(CREDENTIAL_ID)

        coEvery { mockFetchAndSaveCredential(credentialOffer = mockCredentialOffer) } returns Ok(deferredCredential)

        val result = useCase(INVITATION_URI).assertSuccessType(ProcessInvitationResult.DeferredCredential::class)

        assertEquals(deferredCredential.credentialId, result.credentialId)

        coVerifyOrder {
            mockValidateInvitation(INVITATION_URI)
            mockFetchAndSaveCredential(mockCredentialOffer)
        }
    }

    private fun setupDefaultMocks() {
        coEvery { mockValidateInvitation(INVITATION_URI) } returns Ok(mockCredentialOffer)
        coEvery { mockFetchAndSaveCredential(mockCredentialOffer) } returns Ok(FetchCredentialResult.Credential(CREDENTIAL_ID))
    }

    private companion object {
        const val INVITATION_URI = "invitationUri"
        const val CREDENTIAL_ID = 1L
    }
}
