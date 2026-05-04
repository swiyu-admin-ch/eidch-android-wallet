package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestError
import ch.admin.foitt.openid4vc.domain.repository.PresentationRequestRepository
import ch.admin.foitt.openid4vc.domain.usecase.FetchPresentationRequest
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockPresentationRequest
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.net.URL

class FetchPresentationRequestImplTest {

    private val testUrl = URL("https://example.com")

    @MockK
    private lateinit var mockPresentationRequestRepository: PresentationRequestRepository

    private lateinit var useCase: FetchPresentationRequest

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        useCase = FetchPresentationRequestImpl(
            presentationRequestRepository = mockPresentationRequestRepository,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Fetching a valid presentation request jwt succeeds`(): Unit = runTest {
        coEvery {
            mockPresentationRequestRepository.fetchPresentationRequest(any())
        } returns Ok(MockPresentationRequest.validJwt)

        val expectedJwt = Jwt(MockPresentationRequest.validJwt)

        val result = useCase(testUrl).assertOk()

        assertEquals(expectedJwt.payloadString, result.payloadString)

        coVerifyOrder {
            mockPresentationRequestRepository.fetchPresentationRequest(any())
        }
    }

    @Test
    fun `A failed presentationRequestRepository call return an error`(): Unit = runTest {
        coEvery {
            mockPresentationRequestRepository.fetchPresentationRequest(any())
        } returns Err(PresentationRequestError.NetworkError)

        useCase(testUrl).assertErrorType(PresentationRequestError.NetworkError::class)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "abc",
            "aaa.bbb.ccc",
            "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmF.5EQDWn",
        ]
    )
    fun `An invalid jwt presentation request return an error`(wrongJwt: String): Unit = runTest {
        coEvery { mockPresentationRequestRepository.fetchPresentationRequest(any()) } returns Ok(wrongJwt)

        useCase(testUrl).assertErrorType(PresentationRequestError.Unexpected::class)
    }
}
