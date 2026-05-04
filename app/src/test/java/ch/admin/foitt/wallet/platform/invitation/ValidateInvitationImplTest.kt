package ch.admin.foitt.wallet.platform.invitation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOffer
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.Grant
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.CREDENTIAL_ISSUER
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestWithRaw
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.GetCredentialOfferFromUri
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.GetPresentationRequestFromUri
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.ValidateInvitation
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.implementation.ValidateInvitationImpl
import ch.admin.foitt.wallet.util.assertSuccessType
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.get
import com.github.michaelbull.result.getError
import io.mockk.MockKAnnotations
import io.mockk.Ordering
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ValidateInvitationImplTest {

    @MockK
    private lateinit var mockGetCredentialOfferFromUri: GetCredentialOfferFromUri

    @MockK
    private lateinit var mockGetPresentationRequestFromUri: GetPresentationRequestFromUri

    private val mockCredentialOffer = CredentialOffer(
        credentialIssuer = CREDENTIAL_ISSUER,
        credentialConfigurationIds = listOf(),
        grants = Grant(),
    )

    @MockK
    private lateinit var mockPresentationRequestWithRaw: PresentationRequestWithRaw

    private lateinit var validateInvitationUseCase: ValidateInvitation

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        validateInvitationUseCase = ValidateInvitationImpl(
            getCredentialOfferFromUri = mockGetCredentialOfferFromUri,
            getPresentationRequestFromUri = mockGetPresentationRequestFromUri,
        )

        coEvery { mockGetCredentialOfferFromUri.invoke(uri = any()) } returns Ok(mockCredentialOffer)
        coEvery { mockGetPresentationRequestFromUri.invoke(uri = any()) } returns Ok(mockPresentationRequestWithRaw)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `a valid VC invitation scheme should trigger the credential offer validation`() = runTest {
        val input = "openid-credential-offer://whatever"
        val useCaseResult = validateInvitationUseCase(input = input)

        coVerify(ordering = Ordering.ORDERED) {
            mockGetCredentialOfferFromUri.invoke(uri = any())
        }

        coVerify(exactly = 0) {
            mockGetPresentationRequestFromUri.invoke(uri = any())
        }

        assertEquals(mockCredentialOffer, useCaseResult.get())
    }

    @Test
    fun `an https scheme should trigger the presentationRequest validation`() = runTest {
        val input = "https://whatever"
        val useCaseResult = validateInvitationUseCase(input = input)

        coVerify(ordering = Ordering.ORDERED) {
            mockGetPresentationRequestFromUri.invoke(uri = any())
        }

        coVerify(exactly = 0) {
            mockGetCredentialOfferFromUri.invoke(uri = any())
        }

        val presentationRequestWithRaw = useCaseResult.assertSuccessType(PresentationRequestWithRaw::class)
        assertEquals(mockPresentationRequestWithRaw, presentationRequestWithRaw)
    }

    @Test
    fun `unsupported URI should return an error`() = runTest {
        assertTrue(
            validateInvitationUseCase(input = "").getError() is InvitationError.UnknownSchema,
            "empty input should return an error"
        )

        assertTrue(
            validateInvitationUseCase(input = " http//blah").getError() is InvitationError.InvalidUri,
            "input without a valid uri should return an error"
        )

        assertTrue(
            validateInvitationUseCase(input = "unknown-schema://example.org").getError() is InvitationError.UnknownSchema,
            "input without a supported schema should return an error"
        )
    }

    @Test
    fun `a credential offer validation failure should return an error`() = runTest {
        coEvery {
            mockGetCredentialOfferFromUri.invoke(uri = any())
        } returns Err(InvitationError.CredentialOfferDeserializationFailed(Throwable()))

        val input = "openid-credential-offer://whatever"
        val useCaseResult = validateInvitationUseCase(input = input)

        coVerify(ordering = Ordering.ORDERED) {
            mockGetCredentialOfferFromUri.invoke(uri = any())
        }

        assertTrue(useCaseResult.getError() is InvitationError)
    }

    @Test
    fun `a presentation request validation failure should return an error`() = runTest {
        coEvery {
            mockGetPresentationRequestFromUri.invoke(uri = any())
        } returns Err(InvitationError.InvalidPresentationRequest)

        val input = "https://whatever"
        val useCaseResult = validateInvitationUseCase(input = input)

        coVerify(ordering = Ordering.ORDERED) {
            mockGetPresentationRequestFromUri.invoke(uri = any())
        }

        assertTrue(useCaseResult.getError() is InvitationError)
    }
}
