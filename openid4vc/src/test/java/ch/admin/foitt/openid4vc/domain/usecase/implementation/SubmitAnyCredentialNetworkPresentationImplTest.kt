package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseConfig
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseParam
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseType
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestError
import ch.admin.foitt.openid4vc.domain.repository.PresentationRequestRepository
import ch.admin.foitt.openid4vc.util.assertErr
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL

class SubmitAnyCredentialNetworkPresentationImplTest {

    @MockK
    private lateinit var mockPresentationRequestRepository: PresentationRequestRepository

    @MockK
    private lateinit var mockAuthorizationRequest: AuthorizationRequest

    private lateinit var useCase: SubmitAnyCredentialNetworkPresentationImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = SubmitAnyCredentialNetworkPresentationImpl(
            presentationRequestRepository = mockPresentationRequestRepository,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Submitting a DIF presentation just runs`() = runTest {
        every { mockAuthorizationRequest.responseUri } returns RESPONSE_URI
        coEvery {
            mockPresentationRequestRepository.submitPresentation(
                url = URL(RESPONSE_URI),
                authorizationResponseConfig = authorizationResponseConfigDCQL,
            )
        } returns Ok(Unit)

        useCase(
            authorizationRequest = mockAuthorizationRequest,
            authorizationResponseConfig = authorizationResponseConfigDCQL,
        ).assertOk()

        coVerify(exactly = 1) {
            mockPresentationRequestRepository.submitPresentation(URL(RESPONSE_URI), authorizationResponseConfigDCQL)
        }
    }

    @Test
    fun `Submitting a presentation with an invalid response uri returns an error`() = runTest {
        every { mockAuthorizationRequest.responseUri } returns "invalid"

        useCase(
            authorizationRequest = mockAuthorizationRequest,
            authorizationResponseConfig = authorizationResponseConfigDCQL,
        ).assertErrorType(PresentationRequestError.Unexpected::class)
        coVerify(exactly = 0) { mockPresentationRequestRepository.submitPresentation(any(), any()) }
    }

    @Test
    fun `Submitting a presentation maps errors from submitting presentation`() = runTest {
        val exception = IllegalStateException()
        every { mockAuthorizationRequest.responseUri } returns RESPONSE_URI
        coEvery {
            mockPresentationRequestRepository.submitPresentation(any(), any())
        } returns Err(PresentationRequestError.Unexpected(exception))

        val result = useCase(
            authorizationRequest = mockAuthorizationRequest,
            authorizationResponseConfig = authorizationResponseConfigDCQL,
        )

        val error = result.assertErrorType(PresentationRequestError.Unexpected::class)
        assertEquals(exception, error.throwable)
    }

    @Test
    fun `Submitting a presentation maps network errors from repository`() = runTest {
        every { mockAuthorizationRequest.responseUri } returns RESPONSE_URI
        coEvery {
            mockPresentationRequestRepository.submitPresentation(any(), any())
        } returns Err(PresentationRequestError.NetworkError)

        val result = useCase(
            authorizationRequest = mockAuthorizationRequest,
            authorizationResponseConfig = authorizationResponseConfigDCQL,
        )

        assertEquals(PresentationRequestError.NetworkError, result.assertErr())
    }

    private val authorizationResponseConfigDCQL = AuthorizationResponseConfig(
        type = AuthorizationResponseType.DCQL,
        params = mapOf(
            AuthorizationResponseParam.RESPONSE to AUTHORIZATION_RESPONSE_JWE,
        )
    )

    private companion object {
        const val RESPONSE_URI = "https://example.com"
        const val AUTHORIZATION_RESPONSE_JWE = "jwe"
    }
}
