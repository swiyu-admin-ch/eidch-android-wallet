package ch.admin.foitt.wallet.platform.invitation

import ch.admin.foitt.wallet.platform.appSetupState.domain.usecase.GetFirstCredentialWasAdded
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestWithRaw
import ch.admin.foitt.wallet.platform.credentialPresentation.mock.MockPresentationRequest
import ch.admin.foitt.wallet.platform.invitation.domain.model.ProcessInvitationResult
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.HandleInvitationProcessingSuccess
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.implementation.HandleInvitationProcessingSuccessImpl
import ch.admin.foitt.wallet.platform.messageEvents.domain.repository.CredentialOfferEventRepository
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import io.mockk.MockKAnnotations
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class HandleInvitationProcessingSuccessTest {

    @MockK
    private lateinit var mockNavigationManager: NavigationManager

    @MockK
    private lateinit var mockCredentialOfferEventRepository: CredentialOfferEventRepository

    @MockK
    private lateinit var mockFirstCredentialWasAdded: GetFirstCredentialWasAdded

    private lateinit var handleInvitationProcessingSuccess: HandleInvitationProcessingSuccess

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { mockNavigationManager.replaceCurrentWith(any()) } just runs
        coEvery { mockCredentialOfferEventRepository.setEvent(any()) } just runs
        coEvery { mockFirstCredentialWasAdded() } returns true

        handleInvitationProcessingSuccess = HandleInvitationProcessingSuccessImpl(
            navManager = mockNavigationManager,
            credentialOfferEventRepository = mockCredentialOfferEventRepository,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `execution navigates to the defined screen`() = runTest {
        definedSuccessDestinations.forEach { (successResult, destination) ->
            handleInvitationProcessingSuccess(successResult).navigate()

            coVerify(exactly = 1) {
                mockNavigationManager.replaceCurrentWith(destination)
                mockNavigationManager.replaceCurrentWith(destination)
            }
            clearMocks(mockNavigationManager, answers = false)
        }
    }

    companion object {
        private val mockPresentationRequest = MockPresentationRequest.authorizationRequest
        private val mockCredentialOfferResult = ProcessInvitationResult.CredentialOffer(0L)
        private const val RAW_JWT = "rawJwt"

        private val mockCompatibleCredential = CompatibleCredential(
            credentialId = mockCredentialOfferResult.credentialId,
            requestedFields = listOf(),
        )

        private val mockPresentationRequestWithRaw = PresentationRequestWithRaw(
            mockPresentationRequest,
            RAW_JWT,
        )

        private val mockPresentationRequestResult = ProcessInvitationResult.PresentationRequest(
            mockCompatibleCredential,
            mockPresentationRequestWithRaw,
        )

        private val mockPresentationRequestListResult = ProcessInvitationResult.PresentationRequestCredentialList(
            setOf(mockCompatibleCredential),
            mockPresentationRequestWithRaw,
        )

        private val definedSuccessDestinations: Map<ProcessInvitationResult, Destination> = mapOf(
            mockCredentialOfferResult to Destination.CredentialOfferScreen(mockCredentialOfferResult.credentialId),
            mockPresentationRequestResult to Destination.PresentationRequestScreen(
                compatibleCredential = mockPresentationRequestResult.credential,
                presentationRequestWithRaw = mockPresentationRequestResult.request,
            ),
            mockPresentationRequestListResult to Destination.PresentationCredentialListScreen(
                compatibleCredentials = mockPresentationRequestListResult.credentials,
                presentationRequestWithRaw = mockPresentationRequestListResult.request,
            ),
        )
    }
}
