package ch.admin.foitt.wallet.platform.proximity

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.RequestObject
import ch.admin.foitt.swiyu.shared.proximity.ProximityState
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CredentialPresentationError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestWithRaw
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ProcessPresentationRequestResult
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ProximityEngagementError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ProximitySubmissionError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.VerificationProcessType
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ProcessPresentationRequest
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ValidatePresentationRequest
import ch.admin.foitt.wallet.platform.credentialPresentation.mock.MockPresentationRequest
import ch.admin.foitt.wallet.platform.proximity.domain.model.ProximityEngagementEvent
import ch.admin.foitt.wallet.platform.proximity.domain.model.ProximityEngagementUpdate
import ch.admin.foitt.wallet.platform.proximity.domain.model.domain.repository.ProximityRepository
import ch.admin.foitt.wallet.platform.proximity.domain.usecase.GetProximityRepositoryForScope
import ch.admin.foitt.wallet.platform.proximity.domain.usecase.ProximityEngagement
import ch.admin.foitt.wallet.platform.proximity.domain.usecase.implementation.ProximityEngagementImpl
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertSuccessType
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@Suppress("UnusedFlow")
class ProximityEngagementImplTest {
    @MockK
    private lateinit var mockGetProximityRepositoryForScope: GetProximityRepositoryForScope

    @MockK
    private lateinit var mockProximityRepository: ProximityRepository

    @MockK
    private lateinit var mockValidatePresentationRequest: ValidatePresentationRequest

    @MockK
    private lateinit var mockProcessPresentationRequest: ProcessPresentationRequest

    @MockK
    private lateinit var mockPresentationRequestWithRaw: PresentationRequestWithRaw

    private lateinit var useCase: ProximityEngagement

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { mockProximityRepository.state } returns MutableStateFlow(ProximityState.Initial)

        every {
            mockGetProximityRepositoryForScope()
        } returns mockProximityRepository

        useCase = ProximityEngagementImpl(
            validatePresentationRequest = mockValidatePresentationRequest,
            processPresentationRequest = mockProcessPresentationRequest,
            getProximityRepositoryForScope = mockGetProximityRepositoryForScope
        )

        coEvery {
            mockValidatePresentationRequest(
                verificationProcessType = any(),
                requestObject = any()
            )
        } returns Ok(mockPresentationRequestWithRaw)
        coEvery { mockProcessPresentationRequest(any()) } returns Ok(
            ProcessPresentationRequestResult.CredentialList(
                credentials = emptySet(),
                presentationRequest = mockPresentationRequestWithRaw
            )
        )
        coEvery { mockProcessPresentationRequest(mockPresentationRequestWithRaw) } returns Ok(
            ProcessPresentationRequestResult.CredentialList(
                credentials = emptySet(),
                presentationRequest = mockPresentationRequestWithRaw,
            )
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Engagement without QR code starts normal engagement and maps qr code update`() = runTest {
        val update = ProximityEngagementUpdate.QrCode(QR_CODE)
        every { mockProximityRepository.startEngagement() } returns flowOf(Ok(update))

        val result = useCase().first()

        val event = result.assertSuccessType(ProximityEngagementEvent.QrCode::class)
        assertEquals(ProximityEngagementEvent.QrCode(QR_CODE), event)
        verify(exactly = 1) { mockProximityRepository.startEngagement() }
        verify(exactly = 0) { mockProximityRepository.startEngagementReverse(any()) }
    }

    @Test
    fun `Engagement with QR code starts reverse engagement`() = runTest {
        val update = ProximityEngagementUpdate.Request(MockPresentationRequest.VALID_JWT)
        every { mockProximityRepository.startEngagementReverse(QR_CODE) } returns flowOf(Ok(update))

        val result = useCase(QR_CODE).first()

        result.assertSuccessType(ProximityEngagementEvent.Request::class)
        verify(exactly = 1) { mockProximityRepository.startEngagementReverse(QR_CODE) }
        verify(exactly = 0) { mockProximityRepository.startEngagement() }
    }

    @Test
    fun `Request update is validated processed and mapped to request event`() = runTest {
        val update = ProximityEngagementUpdate.Request(MockPresentationRequest.VALID_JWT)
        every { mockProximityRepository.startEngagement() } returns flowOf(Ok(update))
        val requestObjectSlot = slot<RequestObject>()

        useCase().first().assertSuccessType(ProximityEngagementEvent.Request::class)

        coVerify(exactly = 1) {
            mockValidatePresentationRequest(
                verificationProcessType = VerificationProcessType.PROXIMITY,
                requestObject = capture(requestObjectSlot),
            )
        }
        coVerify(exactly = 1) { mockProcessPresentationRequest(mockPresentationRequestWithRaw) }
        assertEquals(MockPresentationRequest.VALID_JWT, requestObjectSlot.captured.jwt.rawJwt)
        assertNull(requestObjectSlot.captured.clientId)
        assertNull(requestObjectSlot.captured.redirectUri)
    }

    @Test
    fun `Submission disconnected error is mapped`() = runTest {
        every { mockProximityRepository.startEngagement() } returns flowOf(Err(ProximitySubmissionError.Failed()))

        val result = useCase().first()

        result.assertErrorType(ProximityEngagementError.Unexpected::class)
    }

    @Test
    fun `Validation error is mapped and processing is not called`() = runTest {
        val update = ProximityEngagementUpdate.Request(request = MockPresentationRequest.VALID_JWT)
        every { mockProximityRepository.startEngagement() } returns flowOf(Ok(update))
        coEvery {
            mockValidatePresentationRequest(
                verificationProcessType = any(),
                requestObject = any(),
            )
        } returns Err(CredentialPresentationError.NetworkError)

        val result = useCase().first()

        result.assertErrorType(ProximityEngagementError.Disconnected::class)
        coVerify(exactly = 0) { mockProcessPresentationRequest(any()) }
    }

    @Test
    fun `Processing error is mapped`() = runTest {
        val throwable = IllegalStateException("processing failed")
        val update = ProximityEngagementUpdate.Request(request = MockPresentationRequest.VALID_JWT)
        every { mockProximityRepository.startEngagement() } returns flowOf(Ok(update))
        coEvery { mockProcessPresentationRequest(mockPresentationRequestWithRaw) } returns Err(
            CredentialPresentationError.Unexpected(throwable)
        )

        val result = useCase().first()
        val error = result.assertErrorType(ProximityEngagementError.Unexpected::class)

        assertEquals(throwable, error.throwable)
    }

    companion object {
        private const val QR_CODE = "QrCode"
    }
}
