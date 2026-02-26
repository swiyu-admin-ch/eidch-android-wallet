package ch.admin.foitt.wallet.platform.eIdApplicationProcess

import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AttestationError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.RequestClientAttestation
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.WalletPairingState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.WalletPairingStateResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.SIdRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation.WalletPairingStatusImpl
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

class WalletPairingStatusImplTest {

    @MockK
    private lateinit var mockSIdRepository: SIdRepository

    @MockK
    private lateinit var mockRequestClientAttestation: RequestClientAttestation

    @MockK
    private lateinit var mockClientAttestation: ClientAttestation
    private lateinit var useCase: WalletPairingStatusImpl

    private val caseId = "case_id"
    private val pairingId = "pairing_id"

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = WalletPairingStatusImpl(mockSIdRepository, mockRequestClientAttestation)

        coEvery { mockRequestClientAttestation(any(), any()) } returns Ok(mockClientAttestation)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `invoke returns success when attestation and repository succeed`() = runTest {
        val stateResponse = WalletPairingStateResponse(state = WalletPairingState.ACCEPTED)

        coEvery {
            mockSIdRepository.getWalletPairingState(caseId, pairingId, mockClientAttestation)
        } returns Ok(stateResponse)

        val result = useCase(caseId, pairingId)

        result.assertOk()
    }

    @Test
    fun `A client attestation error is propagated`() = runTest {
        val exception = Exception("testException")
        coEvery { mockRequestClientAttestation(any(), any()) } returns Err(AttestationError.Unexpected(exception))

        val result = useCase(caseId, pairingId)
        val error = result.assertErrorType(EIdRequestError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    @Test
    fun `A pair wallet state failure is propagated`() = runTest {
        val exception = Exception("my exception")
        coEvery {
            mockSIdRepository.getWalletPairingState(
                caseId = any(),
                walletPairingId = any(),
                clientAttestation = any(),
            )
        } returns Err(EIdRequestError.Unexpected(exception))

        val error = useCase(caseId, pairingId).assertErrorType(EIdRequestError.Unexpected::class)
        assertEquals(exception, error.cause)
    }
}
