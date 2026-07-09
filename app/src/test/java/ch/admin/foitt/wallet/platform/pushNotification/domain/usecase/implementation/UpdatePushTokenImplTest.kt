package ch.admin.foitt.wallet.platform.pushNotification.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestationPoP
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestCase
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.IdentityType
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestCaseRepository
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.PushClientAttestation
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.PushNotificationError
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

class UpdatePushTokenImplTest {

    @MockK
    private lateinit var mockEIdRequestCaseRepository: EIdRequestCaseRepository

    @MockK
    private lateinit var mockPushDeviceTokenRepository: PushDeviceTokenRepository

    @MockK
    private lateinit var mockPushNotificationRepository: PushNotificationRepository

    @MockK
    private lateinit var mockGeneratePushClientAttestation: GeneratePushClientAttestation

    @MockK
    private lateinit var mockPushClientAttestation: PushClientAttestation

    @MockK
    private lateinit var mockClientAttestation: ClientAttestation

    @MockK
    private lateinit var mockClientAttestationPoP: ClientAttestationPoP

    private val testSafeJson = SafeJsonTestInstance.safeJson

    private lateinit var useCase: UpdatePushTokenImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = UpdatePushTokenImpl(
            eIdRequestCaseRepository = mockEIdRequestCaseRepository,
            pushDeviceTokenRepository = mockPushDeviceTokenRepository,
            pushNotificationRepository = mockPushNotificationRepository,
            generatePushClientAttestation = mockGeneratePushClientAttestation,
            safeJson = testSafeJson,
        )

        every { mockPushClientAttestation.attestation } returns mockClientAttestation
        every { mockPushClientAttestation.pop } returns mockClientAttestationPoP

        coEvery { mockPushDeviceTokenRepository.fetchToken() } returns Ok(NEW_PUSH_TOKEN)
        coEvery { mockEIdRequestCaseRepository.getEIdRequestCasesWithPushId() } returns Ok(listOf(CASE_WITH_PUSH_ID))
        coEvery { mockGeneratePushClientAttestation(any()) } returns Ok(mockPushClientAttestation)
        coEvery {
            mockPushNotificationRepository.updatePushDeviceToken(any(), any(), any())
        } returns Ok(Unit)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `No registered cases skips network update`() = runTest {
        coEvery { mockEIdRequestCaseRepository.getEIdRequestCasesWithPushId() } returns Ok(emptyList())

        useCase().assertOk()

        coVerify(exactly = 0) {
            mockGeneratePushClientAttestation(any())
            mockPushNotificationRepository.updatePushDeviceToken(any(), any(), any())
        }
    }

    @Test
    fun `Registered cases sends update to backend`() = runTest {
        useCase().assertOk()

        coVerify {
            mockGeneratePushClientAttestation(any())
            mockPushNotificationRepository.updatePushDeviceToken(
                mockClientAttestation,
                mockClientAttestationPoP,
                any()
            )
        }
    }

    @Test
    fun `Failing to fetch device token fails update`() = runTest {
        coEvery {
            mockPushDeviceTokenRepository.fetchToken()
        } returns Err(PushNotificationError.Unexpected(Exception()))

        useCase().assertErr()

        coVerify(exactly = 0) {
            mockEIdRequestCaseRepository.getEIdRequestCasesWithPushId()
            mockGeneratePushClientAttestation(any())
            mockPushNotificationRepository.updatePushDeviceToken(any(), any(), any())
        }
    }

    @Test
    fun `Failing to fetch cases with push IDs fails update`() = runTest {
        coEvery {
            mockEIdRequestCaseRepository.getEIdRequestCasesWithPushId()
        } returns Err(EIdRequestError.Unexpected(Exception()))

        useCase().assertErr()

        coVerify(exactly = 0) {
            mockGeneratePushClientAttestation(any())
            mockPushNotificationRepository.updatePushDeviceToken(any(), any(), any())
        }
    }

    @Test
    fun `Failing to generate push client attestation fails update`() = runTest {
        coEvery {
            mockGeneratePushClientAttestation(any())
        } returns Err(PushNotificationError.Unexpected(Exception()))

        useCase().assertErr()

        coVerify(exactly = 0) {
            mockPushNotificationRepository.updatePushDeviceToken(any(), any(), any())
        }
    }

    @Test
    fun `Failing backend token update fails use case`() = runTest {
        coEvery {
            mockPushNotificationRepository.updatePushDeviceToken(any(), any(), any())
        } returns Err(PushNotificationError.Unexpected(Exception()))

        useCase().assertErr()
    }

    private companion object {
        const val NEW_PUSH_TOKEN = "new-push-token"
        const val PUSH_ID = "test-push-id"

        val CASE_WITH_PUSH_ID = EIdRequestCase(
            id = "test-case-id",
            rawMrz = "raw-mrz",
            documentNumber = "doc-number",
            selectedDocumentType = IdentityType.SWISS_IDK,
            firstName = "Test",
            lastName = "User",
            pushId = PUSH_ID,
        )
    }
}
