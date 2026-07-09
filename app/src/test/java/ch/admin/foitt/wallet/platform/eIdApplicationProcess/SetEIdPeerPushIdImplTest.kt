package ch.admin.foitt.wallet.platform.eIdApplicationProcess

import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AttestationError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestationPoP
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.GenerateProofOfPossession
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.RequestClientAttestation
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.SIdChallengeResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.SIdRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation.SetEIdPeerPushIdImpl
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.util.SafeJsonTestInstance
import ch.admin.foitt.wallet.util.assertErr
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SetEIdPeerPushIdImplTest {

    @MockK
    private lateinit var mockSIdRepository: SIdRepository

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

    private val testSafeJson = SafeJsonTestInstance.safeJson
    private val mockSIdUrl = "https://example.com"
    private val mockSIdChallenge = SIdChallengeResponse("someChallenge")

    private lateinit var useCase: SetEIdPeerPushIdImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = SetEIdPeerPushIdImpl(
            sIdRepository = mockSIdRepository,
            requestClientAttestation = mockRequestClientAttestation,
            generateProofOfPossession = mockGenerateProofOfPossession,
            environmentSetupRepository = mockEnvironmentSetupRepository,
            safeJson = testSafeJson,
        )

        coEvery { mockRequestClientAttestation(any(), any()) } returns Ok(mockClientAttestation)
        coEvery { mockSIdRepository.fetchChallenge() } returns Ok(mockSIdChallenge)
        coEvery {
            mockGenerateProofOfPossession(
                clientAttestation = any(),
                challenge = any(),
                audience = any(),
                requestBody = any(),
            )
        } returns Ok(mockClientAttestationPoP)
        coEvery { mockEnvironmentSetupRepository.sidBackendUrl } returns mockSIdUrl
        coEvery {
            mockSIdRepository.setPeerPushId(
                caseId = any(),
                clientAttestation = any(),
                clientAttestationPoP = any(),
                request = any(),
            )
        } returns Ok(Unit)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Success follows correct step order`() = runTest {
        useCase(CASE_ID, PUSH_ID).assertOk()

        coVerifyOrder {
            mockRequestClientAttestation(any(), any())
            mockSIdRepository.fetchChallenge()
            mockGenerateProofOfPossession(
                clientAttestation = mockClientAttestation,
                challenge = mockSIdChallenge.challenge,
                audience = mockSIdUrl,
                requestBody = any(),
            )
            mockSIdRepository.setPeerPushId(
                caseId = CASE_ID,
                clientAttestation = mockClientAttestation,
                clientAttestationPoP = mockClientAttestationPoP,
                request = any(),
            )
        }
    }

    @Test
    fun `Failing client attestation fails push ID registration`() = runTest {
        coEvery {
            mockRequestClientAttestation(any(), any())
        } returns Err(AttestationError.Unexpected(Exception()))

        useCase(CASE_ID, PUSH_ID).assertErr()

        coVerify(exactly = 0) {
            mockSIdRepository.fetchChallenge()
            mockGenerateProofOfPossession(any(), any(), any(), any())
            mockSIdRepository.setPeerPushId(any(), any(), any(), any())
        }
    }

    @Test
    fun `Failing challenge fetch fails push ID registration`() = runTest {
        coEvery {
            mockSIdRepository.fetchChallenge()
        } returns Err(EIdRequestError.NetworkError)

        useCase(CASE_ID, PUSH_ID).assertErr()

        coVerify(exactly = 0) {
            mockGenerateProofOfPossession(any(), any(), any(), any())
            mockSIdRepository.setPeerPushId(any(), any(), any(), any())
        }
    }

    @Test
    fun `Failing PoP generation fails push ID registration`() = runTest {
        coEvery {
            mockGenerateProofOfPossession(any(), any(), any(), any())
        } returns Err(AttestationError.Unexpected(Exception()))

        useCase(CASE_ID, PUSH_ID).assertErr()

        coVerify(exactly = 0) {
            mockSIdRepository.setPeerPushId(any(), any(), any(), any())
        }
    }

    @Test
    fun `Fails on push ID registration at issuer`() = runTest {
        coEvery {
            mockSIdRepository.setPeerPushId(any(), any(), any(), any())
        } returns Err(EIdRequestError.NetworkError)

        useCase(CASE_ID, PUSH_ID).assertErr()
    }

    private companion object {
        const val CASE_ID = "test-case-id"
        const val PUSH_ID = "test-push-id"
    }
}
