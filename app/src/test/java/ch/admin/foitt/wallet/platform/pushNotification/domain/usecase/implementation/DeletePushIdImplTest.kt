package ch.admin.foitt.wallet.platform.pushNotification.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestationPoP
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.PushClientAttestation
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.PushNotificationError
import ch.admin.foitt.wallet.platform.pushNotification.domain.repository.PushNotificationRepository
import ch.admin.foitt.wallet.platform.pushNotification.domain.usecase.GeneratePushClientAttestation
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

class DeletePushIdImplTest {

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

    private lateinit var useCase: DeletePushIdImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = DeletePushIdImpl(
            pushNotificationRepository = mockPushNotificationRepository,
            generatePushClientAttestation = mockGeneratePushClientAttestation,
        )

        every { mockPushClientAttestation.attestation } returns mockClientAttestation
        every { mockPushClientAttestation.pop } returns mockClientAttestationPoP

        coEvery { mockGeneratePushClientAttestation(any()) } returns Ok(mockPushClientAttestation)
        coEvery {
            mockPushNotificationRepository.deletePushId(any(), any(), any())
        } returns Ok(Unit)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Successful delete returns ok`() = runTest {
        useCase(PUSH_ID).assertOk()

        coVerify {
            mockGeneratePushClientAttestation(any())
            mockPushNotificationRepository.deletePushId(
                mockClientAttestation,
                mockClientAttestationPoP,
                PUSH_ID,
            )
        }
    }

    @Test
    fun `Failing to generate push client attestation fails delete`() = runTest {
        coEvery {
            mockGeneratePushClientAttestation(any())
        } returns Err(PushNotificationError.Unexpected(Exception()))

        useCase(PUSH_ID).assertErr()

        coVerify(exactly = 0) {
            mockPushNotificationRepository.deletePushId(any(), any(), any())
        }
    }

    @Test
    fun `Failing backend delete fails use case`() = runTest {
        coEvery {
            mockPushNotificationRepository.deletePushId(any(), any(), any())
        } returns Err(PushNotificationError.Unexpected(Exception()))

        useCase(PUSH_ID).assertErr()
    }

    private companion object {
        const val PUSH_ID = "test-push-id"
    }
}
