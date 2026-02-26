package ch.admin.foitt.wallet.platform.eIdApplicationProcess

import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AttestationError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.RequestClientAttestation
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestQueueState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.StateResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.SIdRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.FetchSIdStatus
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation.FetchSIdStatusImpl
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FetchSIdStatusImplTest {

    @MockK
    private lateinit var mockEIdRepository: SIdRepository

    @MockK
    private lateinit var mockRequestClientAttestation: RequestClientAttestation

    @MockK
    private lateinit var mockClientAttestation: ClientAttestation

    private val stateResponse = StateResponse(
        state = EIdRequestQueueState.IN_QUEUING,
        queueInformation = null,
        legalRepresentant = null,
        onlineSessionStartTimeout = null,
        targetWallets = null
    )

    lateinit var fetchSIdStatus: FetchSIdStatus

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        fetchSIdStatus = FetchSIdStatusImpl(
            sIdRepository = mockEIdRepository,
            requestClientAttestation = mockRequestClientAttestation,
        )

        coEvery { mockRequestClientAttestation(any(), any()) } returns Ok(mockClientAttestation)

        coEvery {
            mockEIdRepository.fetchSIdState(
                caseId = any(),
                clientAttestation = mockClientAttestation,
            )
        } returns Ok(stateResponse)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Successfully fetch an eID status`() = runTest {
        val response = fetchSIdStatus("caseID").assertOk()

        assertEquals(EIdRequestQueueState.IN_QUEUING, response.state)
    }

    @Test
    fun `Unsuccessfully fetch an eID status`() = runTest {
        val error = EIdRequestError.Unexpected(
            Exception("myException")
        )
        coEvery { mockEIdRepository.fetchSIdState(any(), any()) } returns Err(error)

        fetchSIdStatus("caseID").assertErrorType(EIdRequestError.Unexpected::class)
    }

    @Test
    fun `A client attestation error is propagated`() = runTest {
        val exception = Exception("testException")
        coEvery { mockRequestClientAttestation(any(), any()) } returns Err(AttestationError.Unexpected(exception))

        val result = fetchSIdStatus("caseId")
        val error = result.assertErrorType(EIdRequestError.Unexpected::class)
        assertEquals(exception, error.cause)
    }
}
