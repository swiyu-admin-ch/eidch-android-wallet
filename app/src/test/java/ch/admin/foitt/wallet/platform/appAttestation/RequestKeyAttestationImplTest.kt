package ch.admin.foitt.wallet.platform.appAttestation

import android.annotation.SuppressLint
import ch.admin.foitt.openid4vc.domain.model.JwkError
import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.JWSKeyPair
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBindingType
import ch.admin.foitt.openid4vc.domain.usecase.CreateJwk
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AttestationChallengeResponse
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AttestationError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.KeyAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.KeyAttestationResponse
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.RequestKeyAttestationError
import ch.admin.foitt.wallet.platform.appAttestation.domain.repository.AppAttestationRepository
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.RequestKeyAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.ValidateKeyAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.implementation.RequestKeyAttestationImpl
import ch.admin.foitt.wallet.platform.appAttestation.domain.util.getBase64CertificateChain
import ch.admin.foitt.wallet.platform.appAttestation.mock.KeyAttestationMocks
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.model.KeyPairError
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.usecase.CreateJWSKeyPairInHardware
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.security.KeyPair

class RequestKeyAttestationImplTest {

    @MockK
    private lateinit var mockAppAttestationRepository: AppAttestationRepository

    @MockK
    private lateinit var mockCreateJWSKeyPairInHardware: CreateJWSKeyPairInHardware

    @MockK
    private lateinit var mockCreateJwk: CreateJwk

    @MockK
    private lateinit var mockValidateKeyAttestation: ValidateKeyAttestation

    private val challengeResponse = AttestationChallengeResponse("challenge")

    private var keyPair: KeyPair = KeyPair(mockk(), mockk())
    private val keyStoreAlias = "keyAlias"
    private val keyAttestationRawJwt = KeyAttestationMocks.jwtSimple01
    private val keyAttestationJwt = Jwt(keyAttestationRawJwt.value)
    private val signingAlgorithm = SigningAlgorithm.ES512
    private val jwsKeyPair = JWSKeyPair(signingAlgorithm, keyPair, keyStoreAlias, KeyBindingType.SOFTWARE)
    private val keyAttestation = KeyAttestation(jwsKeyPair, keyAttestationJwt)
    private val jwk = KeyAttestationMocks.jwkEcP256_02

    private lateinit var useCase: RequestKeyAttestation

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = RequestKeyAttestationImpl(
            attestationRepository = mockAppAttestationRepository,
            createJWSKeyPairInHardware = mockCreateJWSKeyPairInHardware,
            createJwk = mockCreateJwk,
            validateKeyAttestation = mockValidateKeyAttestation,
        )

        coEvery { mockAppAttestationRepository.fetchChallenge() } returns Ok(challengeResponse)

        coEvery { mockCreateJWSKeyPairInHardware(any(), any(), any(), any(), any()) } returns Ok(jwsKeyPair)

        coEvery { mockCreateJwk(any(), any(), any()) } returns Ok(jwk)

        mockkStatic(JWSKeyPair::getBase64CertificateChain)
        coEvery { any<JWSKeyPair>().getBase64CertificateChain() } returns Ok(listOf("base64Certificate"))

        coEvery {
            mockAppAttestationRepository.fetchKeyAttestation(any())
        } returns Ok(KeyAttestationResponse(keyAttestationRawJwt))

        coEvery { mockValidateKeyAttestation(any(), any()) } returns Ok(keyAttestationJwt)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @SuppressLint("CheckResult")
    @Test
    fun `A success returns a valid KeyAttestation and follows specific steps`() = runTest {
        val result: Result<KeyAttestation, RequestKeyAttestationError> = useCase()
        val resultKeyAttestation = result.assertOk()

        assert(resultKeyAttestation == keyAttestation)

        coVerifyOrder {
            mockAppAttestationRepository.fetchChallenge()
            mockCreateJWSKeyPairInHardware(any(), any(), any(), any(), any())
            mockCreateJwk(jwsKeyPair.keyPair, jwsKeyPair.algorithm, any())
            mockAppAttestationRepository.fetchKeyAttestation(any())
            mockValidateKeyAttestation(any(), any())
        }
    }

    @Test
    fun `A network failure while getting the challenge is propagated`() = runTest {
        coEvery { mockAppAttestationRepository.fetchChallenge() } returns Err(AttestationError.NetworkError)
        val result: Result<KeyAttestation, RequestKeyAttestationError> = useCase()
        result.assertErrorType(AttestationError.NetworkError::class)

        coVerify(exactly = 1) {
            mockAppAttestationRepository.fetchChallenge()
        }

        coVerify(exactly = 0) {
            mockCreateJWSKeyPairInHardware(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `A JWSKeyPair creation failure is propagated`() = runTest {
        val exception = Exception("myException")
        coEvery {
            mockCreateJWSKeyPairInHardware.invoke(any(), any(), any(), any(), any())
        } returns Err(KeyPairError.Unexpected(exception))
        val result: Result<KeyAttestation, RequestKeyAttestationError> = useCase()
        val error = result.assertErrorType(AttestationError.Unexpected::class)
        assertEquals(exception, error.throwable)
    }

    @Test
    fun `A Jwk string creation failure is propagated`() = runTest {
        val exception = Exception("myException")
        coEvery { mockCreateJwk.invoke(any(), any(), any()) } returns Err(JwkError.Unexpected(exception))
        val result: Result<KeyAttestation, RequestKeyAttestationError> = useCase()
        val error = result.assertErrorType(AttestationError.Unexpected::class)
        assertEquals(exception, error.throwable)
    }

    @Test
    fun `A certificate creation failure is propagated`() = runTest {
        val exception = Exception("myException")
        coEvery { any<JWSKeyPair>().getBase64CertificateChain() } returns Err(exception)
        val result: Result<KeyAttestation, RequestKeyAttestationError> = useCase()
        val error = result.assertErrorType(AttestationError.Unexpected::class)
        assertEquals(exception, error.throwable)
    }

    @Test
    fun `An attestation fetching failure is propagated`() = runTest {
        val exception = Exception("myException")
        coEvery {
            mockAppAttestationRepository.fetchKeyAttestation(any())
        } returns Err(AttestationError.Unexpected(exception))
        val result: Result<KeyAttestation, RequestKeyAttestationError> = useCase()
        val error = result.assertErrorType(AttestationError.Unexpected::class)
        assertEquals(exception, error.throwable)
    }

    @Test
    fun `An attestation validation failure is propagated`() = runTest {
        val exception = Exception("myException")
        coEvery {
            mockValidateKeyAttestation.invoke(any(), any())
        } returns Err(AttestationError.Unexpected(exception))
        val result: Result<KeyAttestation, RequestKeyAttestationError> = useCase()
        val error = result.assertErrorType(AttestationError.Unexpected::class)
        assertEquals(exception, error.throwable)
    }
}
