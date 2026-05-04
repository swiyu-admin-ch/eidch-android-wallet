package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseErrorBody
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestError
import ch.admin.foitt.openid4vc.domain.repository.PresentationRequestRepository
import ch.admin.foitt.openid4vc.domain.usecase.DeclinePresentation
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DeclinePresentationImplTest {

    @MockK
    private lateinit var mockPresentationRequestRepository: PresentationRequestRepository

    private lateinit var useCase: DeclinePresentation

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery {
            mockPresentationRequestRepository.submitPresentationError(URL, authorizationResponseErrorBody)
        } returns Ok(Unit)

        useCase = DeclinePresentationImpl(
            presentationRequestRepository = mockPresentationRequestRepository,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Successfully submitting a presentation error returns a success`() = runTest {
        useCase(URL, errorType).assertOk()
    }

    @Test
    fun `Error during submission returns the error`() = runTest {
        coEvery {
            mockPresentationRequestRepository.submitPresentationError(URL, authorizationResponseErrorBody)
        } returns Err(PresentationRequestError.NetworkError)

        useCase(URL, errorType).assertErrorType(PresentationRequestError.NetworkError::class)
    }

    private companion object {
        const val URL = "url"
        val errorType = AuthorizationResponseErrorBody.ErrorType.CLIENT_REJECTED
        val authorizationResponseErrorBody = AuthorizationResponseErrorBody(errorType)
    }
}
