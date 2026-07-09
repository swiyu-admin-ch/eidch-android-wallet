package ch.admin.foitt.wallet.platform.pushNotification.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AttestationError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestationPoP
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.GenerateProofOfPossession
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.RequestClientAttestation
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.PushChallengeResponse
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.PushNotificationError
import ch.admin.foitt.wallet.platform.pushNotification.domain.repository.PushNotificationRepository
import ch.admin.foitt.wallet.util.assertErr
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonElement
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GeneratePushClientAttestationImplTest {

    @MockK
    private lateinit var mockPushNotificationRepository: PushNotificationRepository

    @MockK
    private lateinit var mockRequestClientAttestation: RequestClientAttestation

    @MockK
    private lateinit var mockGenerateProofOfPossession: GenerateProofOfPossession

    @MockK
    private lateinit var mockEnvironmentSetupRepository: EnvironmentSetupRepository

    @MockK
    private lateinit var mockClientAttestation: ClientAttestation

    @MockK
    private lateinit var mockClientAttestationPoP: ClientAttestationPoP

    @MockK
    private lateinit var mockRequestBody: JsonElement

    private lateinit var useCase: GeneratePushClientAttestationImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = GeneratePushClientAttestationImpl(
            pushNotificationRepository = mockPushNotificationRepository,
            requestClientAttestation = mockRequestClientAttestation,
            generateProofOfPossession = mockGenerateProofOfPossession,
            environmentSetupRepository = mockEnvironmentSetupRepository,
        )

        every { mockEnvironmentSetupRepository.notificationBackendUrl } returns NOTIFICATION_BACKEND_URL

        coEvery { mockPushNotificationRepository.fetchPushChallenge() } returns Ok(PushChallengeResponse(nonce = CHALLENGE))
        coEvery { mockRequestClientAttestation(any(), any()) } returns Ok(mockClientAttestation)
        coEvery {
            mockGenerateProofOfPossession(
                clientAttestation = mockClientAttestation,
                challenge = CHALLENGE,
                audience = NOTIFICATION_BACKEND_URL,
                requestBody = mockRequestBody,
            )
        } returns Ok(mockClientAttestationPoP)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Success returns PushClientAttestation with correct values`() = runTest {
        val result = useCase(mockRequestBody).assertOk()

        assertEquals(mockClientAttestation, result.attestation)
        assertEquals(mockClientAttestationPoP, result.pop)
    }

    @Test
    fun `Failing to fetch challenge fails attestation generation`() = runTest {
        coEvery {
            mockPushNotificationRepository.fetchPushChallenge()
        } returns Err(PushNotificationError.Unexpected(Exception()))

        useCase(mockRequestBody).assertErr()

        coVerify(exactly = 0) {
            mockRequestClientAttestation(any(), any())
            mockGenerateProofOfPossession(any(), any(), any(), any())
        }
    }

    @Test
    fun `Failing to fetch client attestation fails attestation generation`() = runTest {
        coEvery {
            mockRequestClientAttestation(any(), any())
        } returns Err(AttestationError.Unexpected(Exception()))

        useCase(mockRequestBody).assertErr()

        coVerify(exactly = 0) {
            mockGenerateProofOfPossession(any(), any(), any(), any())
        }
    }

    @Test
    fun `Failing to generate Proof Of Possession fails attestation generation`() = runTest {
        coEvery {
            mockGenerateProofOfPossession(any(), any(), any(), any())
        } returns Err(AttestationError.Unexpected(Exception()))

        useCase(mockRequestBody).assertErr()
    }

    private companion object {
        const val CHALLENGE = "test-challenge"
        const val NOTIFICATION_BACKEND_URL = "https://notifications.example.com"
    }
}
