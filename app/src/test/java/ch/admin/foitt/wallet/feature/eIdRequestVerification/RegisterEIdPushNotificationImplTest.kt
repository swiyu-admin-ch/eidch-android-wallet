package ch.admin.foitt.wallet.feature.eIdRequestVerification

import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.RegisterEIdPushNotification
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.implementation.RegisterEIdPushNotificationImpl
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestationPoP
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestCaseRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.SetEIdPeerPushId
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.PushClientAttestation
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.PushNotificationError
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.PushRegistrationResponse
import ch.admin.foitt.wallet.platform.pushNotification.domain.repository.PushDeviceTokenRepository
import ch.admin.foitt.wallet.platform.pushNotification.domain.repository.PushNotificationRepository
import ch.admin.foitt.wallet.platform.pushNotification.domain.usecase.GeneratePushClientAttestation
import ch.admin.foitt.wallet.util.SafeJsonTestInstance
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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RegisterEIdPushNotificationImplTest {

    @MockK
    private lateinit var mockPushDeviceTokenRepository: PushDeviceTokenRepository

    @MockK
    private lateinit var mockPushNotificationRepository: PushNotificationRepository

    @MockK
    private lateinit var mockEIdRequestCaseRepository: EIdRequestCaseRepository

    @MockK
    private lateinit var mockGeneratePushClientAttestation: GeneratePushClientAttestation

    @MockK
    private lateinit var mockSetEIdPeerPushId: SetEIdPeerPushId

    @MockK
    private lateinit var mockPushClientAttestation: PushClientAttestation

    @MockK
    private lateinit var mockClientAttestation: ClientAttestation

    @MockK
    private lateinit var mockClientAttestationPoP: ClientAttestationPoP

    private val testSafeJson = SafeJsonTestInstance.safeJson

    private lateinit var useCase: RegisterEIdPushNotification

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = RegisterEIdPushNotificationImpl(
            pushDeviceTokenRepository = mockPushDeviceTokenRepository,
            pushNotificationRepository = mockPushNotificationRepository,
            eIdRequestCaseRepository = mockEIdRequestCaseRepository,
            generatePushClientAttestation = mockGeneratePushClientAttestation,
            safeJson = testSafeJson,
            setEIdPeerPushId = mockSetEIdPeerPushId,
        )

        every { mockPushClientAttestation.attestation } returns mockClientAttestation
        every { mockPushClientAttestation.pop } returns mockClientAttestationPoP

        coEvery { mockPushDeviceTokenRepository.fetchToken() } returns Ok(PUSH_TOKEN)
        coEvery { mockGeneratePushClientAttestation(any()) } returns Ok(mockPushClientAttestation)
        coEvery {
            mockPushNotificationRepository.registerPushDeviceToken(any(), any(), any())
        } returns Ok(PushRegistrationResponse(pushId = PUSH_ID))
        coEvery { mockEIdRequestCaseRepository.setPushId(any(), any()) } returns Ok(Unit)
        coEvery { mockSetEIdPeerPushId(any(), any()) } returns Ok(Unit)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Successful registration saves case with push id from response`() = runTest {
        useCase(CASE_ID).assertOk()

        coVerify { mockEIdRequestCaseRepository.setPushId(caseId = CASE_ID, pushId = PUSH_ID) }
        coVerify { mockSetEIdPeerPushId(caseId = CASE_ID, pushId = PUSH_ID) }
    }

    @Test
    fun `Failing fetch of Push Device Token fails registration`() = runTest {
        coEvery {
            mockPushDeviceTokenRepository.fetchToken()
        } returns Err(PushNotificationError.Unexpected(Exception()))

        useCase(CASE_ID).assertErr()

        coVerify(exactly = 0) {
            mockGeneratePushClientAttestation(any())
            mockPushNotificationRepository.registerPushDeviceToken(any(), any(), any())
            mockEIdRequestCaseRepository.setPushId(any(), any())
        }
    }

    @Test
    fun `Failing Push Client Attestation generation fails registration`() = runTest {
        coEvery {
            mockGeneratePushClientAttestation(any())
        } returns Err(PushNotificationError.Unexpected(Exception()))

        useCase(CASE_ID).assertErr()

        coVerify(exactly = 0) {
            mockPushNotificationRepository.registerPushDeviceToken(any(), any(), any())
            mockEIdRequestCaseRepository.setPushId(any(), any())
        }
    }

    @Test
    fun `Failing Push Device Token registration skips case update`() = runTest {
        coEvery {
            mockPushNotificationRepository.registerPushDeviceToken(any(), any(), any())
        } returns Err(PushNotificationError.Unexpected(Exception()))

        useCase(CASE_ID).assertErr()

        coVerify(exactly = 0) {
            mockEIdRequestCaseRepository.setPushId(any(), any())
        }
    }

    @Test
    fun `Failing EIdRequestCase update fails registration`() = runTest {
        coEvery {
            mockEIdRequestCaseRepository.setPushId(any(), any())
        } returns Err(EIdRequestError.Unexpected(Exception()))

        useCase(CASE_ID).assertErr()

        coVerify(exactly = 0) { mockSetEIdPeerPushId(any(), any()) }
    }

    @Test
    fun `Failing to send push ID to issuer fails registration`() = runTest {
        coEvery {
            mockSetEIdPeerPushId(any(), any())
        } returns Err(EIdRequestError.Unexpected(Exception()))

        useCase(CASE_ID).assertErr()
    }

    private companion object {
        const val CASE_ID = "test-case-id"
        const val PUSH_TOKEN = "test-push-token"
        const val PUSH_ID = "test-push-id"
    }
}
