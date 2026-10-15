package ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AttestationError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestationPoP
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.GenerateProofOfPossession
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.RequestClientAttestation
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceChallengeResponse
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceError
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceMetadata
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceRequest
import ch.admin.foitt.wallet.platform.nonCompliance.domain.repository.NonComplianceRepository
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.SubmitNonComplianceReport
import ch.admin.foitt.wallet.util.SafeJsonTestInstance.safeJson
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SubmitNonComplianceReportImplTest {

    @MockK
    private lateinit var mockRequestClientAttestation: RequestClientAttestation

    @MockK
    private lateinit var mockNonComplianceRepository: NonComplianceRepository

    @MockK
    private lateinit var mockGenerateProofOfPossession: GenerateProofOfPossession

    @MockK
    private lateinit var mockEnvironmentSetupRepository: EnvironmentSetupRepository

    @MockK
    private lateinit var mockClientAttestation: ClientAttestation

    @MockK
    private lateinit var mockClientAttestationPoP: ClientAttestationPoP

    private lateinit var useCase: SubmitNonComplianceReport

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = SubmitNonComplianceReportImpl(
            requestClientAttestation = mockRequestClientAttestation,
            nonComplianceRepository = mockNonComplianceRepository,
            safeJson = safeJson,
            generateProofOfPossession = mockGenerateProofOfPossession,
            environmentSetupRepository = mockEnvironmentSetupRepository,
        )

        setupDefaultMocks()
    }

    @Test
    fun `Submitting report returns Ok`() = runTest {
        useCase(request).assertOk()

        coVerify {
            mockRequestClientAttestation()
            mockNonComplianceRepository.fetchChallenge()
            mockGenerateProofOfPossession(mockClientAttestation, POP_CHALLENGE, NON_COMPLIANCE_BASE_URL, any())
            mockNonComplianceRepository.sendReport(mockClientAttestation, mockClientAttestationPoP, request)
        }
    }

    @Test
    fun `Submitting report where client attestation fails returns error`() = runTest {
        coEvery { mockRequestClientAttestation() } returns Err(AttestationError.ValidationError("message"))

        useCase(request).assertErrorType(NonComplianceError.InvalidClientAttestation::class)
    }

    @Test
    fun `Submitting report where fetching challenge fails returns error`() = runTest {
        coEvery {
            mockNonComplianceRepository.fetchChallenge()
        } returns Err(NonComplianceError.Unexpected(IllegalStateException()))

        useCase(request).assertErrorType(NonComplianceError.Unexpected::class)
    }

    @Test
    fun `Submitting report where generating PoP fails returns error`() = runTest {
        coEvery {
            mockGenerateProofOfPossession(any(), any(), any(), any())
        } returns Err(AttestationError.Unexpected(IllegalStateException()))

        useCase(request).assertErrorType(NonComplianceError.Unexpected::class)
    }

    @Test
    fun `Submitting report where sending report fails returns error`() = runTest {
        coEvery {
            mockNonComplianceRepository.sendReport(any(), any(), any())
        } returns Err(NonComplianceError.Unexpected(IllegalStateException()))

        useCase(request).assertErrorType(NonComplianceError.Unexpected::class)
    }

    private fun setupDefaultMocks() {
        coEvery { mockRequestClientAttestation() } returns Ok(mockClientAttestation)
        coEvery { mockNonComplianceRepository.fetchChallenge() } returns Ok(
            mockk<NonComplianceChallengeResponse> {
                every { challenge } returns POP_CHALLENGE
            }
        )
        coEvery { mockEnvironmentSetupRepository.nonComplianceBaseUrl } returns NON_COMPLIANCE_BASE_URL
        coEvery {
            mockGenerateProofOfPossession(any(), any(), any(), any())
        } returns Ok(mockClientAttestationPoP)
        coEvery {
            mockNonComplianceRepository.sendReport(any(), any(), any())
        } returns Ok(Unit)
    }

    private companion object {
        const val NON_COMPLIANCE_BASE_URL = "nonComplianceBaseUrl"
        const val POP_CHALLENGE = "challenge"

        val request = NonComplianceRequest(
            type = "type",
            description = "description",
            metadata = NonComplianceMetadata(),
        )
    }
}
