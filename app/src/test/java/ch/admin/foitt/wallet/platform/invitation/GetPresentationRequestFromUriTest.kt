package ch.admin.foitt.wallet.platform.invitation

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.RequestObject
import ch.admin.foitt.openid4vc.domain.usecase.FetchPresentationRequest
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CredentialPresentationError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestWithRaw
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ValidatePresentationRequest
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.GetPresentationRequestFromUri
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.implementation.GetPresentationRequestFromUriImpl
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.Ordering
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.net.URI
import java.net.URL

class GetPresentationRequestFromUriTest {

    @MockK
    private lateinit var mockFetchPresentationRequest: FetchPresentationRequest

    @MockK
    private lateinit var mockValidatePresentationRequest: ValidatePresentationRequest

    @MockK
    private lateinit var mockJwt: Jwt

    @MockK
    private lateinit var mockPresentationRequestWithRaw: PresentationRequestWithRaw

    private lateinit var getPresentationRequestFromUri: GetPresentationRequestFromUri

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        getPresentationRequestFromUri = GetPresentationRequestFromUriImpl(
            fetchPresentationRequest = mockFetchPresentationRequest,
            validatePresentationRequest = mockValidatePresentationRequest,
        )

        coEvery {
            mockFetchPresentationRequest(url = any())
        } returns Ok(mockJwt)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `A valid https presentation request should return a success`() = runTest {
        coEvery {
            mockFetchPresentationRequest(url = validHttpsRequestUrl)
        } returns Ok(mockJwt)
        coEvery {
            mockValidatePresentationRequest(RequestObject(mockJwt, null, null))
        } returns Ok(mockPresentationRequestWithRaw)

        val result = getPresentationRequestFromUri(validPresentationUri).assertOk()

        assertEquals(mockPresentationRequestWithRaw, result)
    }

    @ParameterizedTest
    @MethodSource("getSuccessMappings")
    fun `A valid non https presentation request should return a success`(input: Pair<URI, URL>) = runTest {
        coEvery {
            mockFetchPresentationRequest(url = input.second)
        } returns Ok(mockJwt)
        coEvery {
            mockValidatePresentationRequest(RequestObject(mockJwt, CLIENT_ID, REDIRECT_URI))
        } returns Ok(mockPresentationRequestWithRaw)

        val result = getPresentationRequestFromUri(input.first).assertOk()

        assertEquals(mockPresentationRequestWithRaw, result)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "openid4vp://?redirect_uri=https%3A%2F%2Frp.example&request_uri=https%3A%2F%2Frp.example%2Fresource_location.jwt",
            "swiyu-verify://?redirect_uri=https%3A%2F%2Frp.example&request_uri=https%3A%2F%2Frp.example%2Fresource_location.jwt",
        ]
    )
    fun `A non https presentation uri must contain a clientId`(uri: String) = runTest {
        getPresentationRequestFromUri(URI(uri)).assertErrorType(InvitationError.InvalidUri::class)

        coVerify(exactly = 0) {
            mockFetchPresentationRequest.invoke(url = any())
        }
    }

    @Test
    fun `An invalid URL should return an error`() = runTest {
        getPresentationRequestFromUri(URI("invalid://invalid.com")).assertErrorType(InvitationError.InvalidUri::class)

        coVerify(exactly = 0) {
            mockFetchPresentationRequest.invoke(url = any())
        }
    }

    @Test
    fun `A repository network error should return an error`() = runTest {
        coEvery {
            mockFetchPresentationRequest(url = any())
        } returns Err(PresentationRequestError.NetworkError)

        getPresentationRequestFromUri(validPresentationUri).assertErrorType(InvitationError.NetworkError::class)

        coVerify(ordering = Ordering.ORDERED) {
            mockFetchPresentationRequest.invoke(url = any())
        }
    }

    @Test
    fun `A presentation unexpected error should return an InvalidInvitation error`() = runTest {
        coEvery {
            mockFetchPresentationRequest(url = any())
        } returns Err(PresentationRequestError.Unexpected(Exception()))

        getPresentationRequestFromUri(validPresentationUri).assertErrorType(InvitationError.InvalidPresentationRequest::class)

        coVerify(ordering = Ordering.ORDERED) {
            mockFetchPresentationRequest.invoke(url = any())
        }
    }

    @Test
    fun `A validation error is mapped`() = runTest {
        coEvery {
            mockFetchPresentationRequest(url = validSwiyuDecodedRequestUrl)
        } returns Ok(mockJwt)
        coEvery {
            mockValidatePresentationRequest(any())
        } returns Err(CredentialPresentationError.Unexpected(IllegalStateException("validation error")))

        getPresentationRequestFromUri(validSwiyuUri).assertErrorType(InvitationError.Unexpected::class)
    }

    companion object {
        const val CLIENT_ID = "clientId"
        const val REDIRECT_URI = "https://rp.example"

        private val validPresentationUri =
            URI("https://example.org/get_request_object/88cf0d95-54c9-465f-9b97-0ba782314700")

        private val validHttpsRequestUrl = validPresentationUri.toURL()

        private val validOIDUri =
            URI(
                "openid4vp://?client_id=$CLIENT_ID&redirect_uri=https%3A%2F%2Frp.example&request_uri=https%3A%2F%2Frp.example%2Fresource_location.jwt"
            )
        private val validOIDDecodedRequestUrl = URL("https://rp.example/resource_location.jwt")
        private val validSwiyuUri =
            URI(
                "swiyu-verify://?client_id=$CLIENT_ID&redirect_uri=https%3A%2F%2Frp.example&request_uri=https%3A%2F%2Frp.example%2Fresource_location.jwt"
            )
        private val validSwiyuDecodedRequestUrl = URL("https://rp.example/resource_location.jwt")

        @JvmStatic
        fun getSuccessMappings() = listOf(
            // Triple of presentation uri, request_uri as URL, expected client_id
            Pair(validOIDUri, validOIDDecodedRequestUrl),
            Pair(validSwiyuUri, validSwiyuDecodedRequestUrl)
        )
    }
}
