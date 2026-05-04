package ch.admin.foitt.wallet.feature.otp.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.otp.domain.model.OtpError
import ch.admin.foitt.wallet.feature.otp.domain.model.OtpRequest
import ch.admin.foitt.wallet.feature.otp.domain.repository.OtpRepository
import ch.admin.foitt.wallet.feature.otp.domain.usecase.RequestOtp
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AttestationChallengeResponse
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AttestationError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestationPoP
import ch.admin.foitt.wallet.platform.appAttestation.domain.repository.AppAttestationRepository
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.GenerateProofOfPossession
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.RequestClientAttestation
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.util.SafeJsonTestInstance
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RequestOtpImplTest {

    @MockK
    private lateinit var mockAppAttestationRepository: AppAttestationRepository

    @MockK
    private lateinit var mockOtpRepository: OtpRepository

    @MockK
    private lateinit var mockRequestClientAttestation: RequestClientAttestation

    @MockK
    private lateinit var mockGenerateProofOfPossession: GenerateProofOfPossession

    @MockK
    private lateinit var mockEnvironmentSetupRepository: EnvironmentSetupRepository

    private val testSafeJson = SafeJsonTestInstance.safeJson

    private lateinit var requestOtp: RequestOtp

    private val mockUrl = "https://example.com"
    private val mockChallenge = AttestationChallengeResponse("someChallenge")
    private val testOtpRequest = OtpRequest(email = "test@example.com")

    @MockK
    private lateinit var mockClientAttestation: ClientAttestation

    @MockK
    private lateinit var mockClientAttestationPoP: ClientAttestationPoP

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        requestOtp = RequestOtpImpl(
            appAttestationRepository = mockAppAttestationRepository,
            otpRepository = mockOtpRepository,
            requestClientAttestation = mockRequestClientAttestation,
            generateProofOfPossession = mockGenerateProofOfPossession,
            environmentSetupRepository = mockEnvironmentSetupRepository,
            safeJson = testSafeJson,
        )

        coEvery { mockRequestClientAttestation(any(), any()) } returns Ok(mockClientAttestation)
        coEvery { mockAppAttestationRepository.fetchChallenge() } returns Ok(mockChallenge)
        coEvery { mockEnvironmentSetupRepository.attestationsServiceUrl } returns mockUrl
        coEvery { mockGenerateProofOfPossession(any(), any(), any(), any()) } returns Ok(mockClientAttestationPoP)
        coEvery { mockOtpRepository.requestOTP(any(), any(), any()) } returns Ok(Unit)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `A success follows specific step`() = runTest {
        requestOtp(testOtpRequest).assertOk()

        coVerifyOrder {
            mockRequestClientAttestation()
            mockAppAttestationRepository.fetchChallenge()
            mockGenerateProofOfPossession(
                clientAttestation = mockClientAttestation,
                challenge = mockChallenge.challenge,
                audience = mockUrl,
                requestBody = any(),
            )
            mockOtpRepository.requestOTP(
                clientAttestation = mockClientAttestation,
                clientAttestationPoP = mockClientAttestationPoP,
                otpRequest = testOtpRequest,
            )
        }
    }

    @Test
    fun `A client attestation error is propagated`() = runTest {
        val exception = Exception("testException")
        coEvery {
            mockRequestClientAttestation(any(), any())
        } returns Err(AttestationError.Unexpected(exception))

        val error = requestOtp(testOtpRequest).assertErrorType(OtpError.Unexpected::class)
        assertEquals(exception, error.throwable)
    }

    @Test
    fun `A challenge fetching failure is propagated`() = runTest {
        coEvery {
            mockAppAttestationRepository.fetchChallenge()
        } returns Err(AttestationError.NetworkError)

        requestOtp(testOtpRequest).assertErrorType(OtpError.NetworkError::class)
    }

    @Test
    fun `A client attestation PoP generation failure is propagated`() = runTest {
        val exception = Exception("my exception")
        coEvery {
            mockGenerateProofOfPossession(any(), any(), any(), any())
        } returns Err(AttestationError.Unexpected(exception))

        val error = requestOtp(testOtpRequest).assertErrorType(OtpError.Unexpected::class)
        assertEquals(exception, error.throwable)
    }

    @Test
    fun `An OTP request failure is propagated`() = runTest {
        coEvery {
            mockOtpRepository.requestOTP(any(), any(), any())
        } returns Err(OtpError.NetworkError)

        requestOtp(testOtpRequest).assertErrorType(OtpError.NetworkError::class)
    }
}
