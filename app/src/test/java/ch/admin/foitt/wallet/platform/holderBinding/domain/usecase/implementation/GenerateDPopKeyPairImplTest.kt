package ch.admin.foitt.wallet.platform.holderBinding.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.VerifiableCredentialParams
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.JWSKeyPair
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.KeyAttestationConfig
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.ProofTypeConfig
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.usecase.GenerateDPoPKeyPair
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.KeyAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.RequestKeyAttestation
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.usecase.CreateJWSKeyPairInSoftware
import ch.admin.foitt.wallet.util.assertOk
import ch.admin.foitt.wallet.util.assertOkNullable
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull

class GenerateDPopKeyPairImplTest {

    @MockK
    private lateinit var mockEnvironmentSetupRepository: EnvironmentSetupRepository

    @MockK
    private lateinit var mockRequestKeyAttestation: RequestKeyAttestation

    @MockK
    private lateinit var mockCreateJWSKeyPairInSoftware: CreateJWSKeyPairInSoftware

    @MockK
    private lateinit var mockVerifiableCredentialParams: VerifiableCredentialParams

    @MockK
    private lateinit var mockHardwareKeyPair: JWSKeyPair

    @MockK
    private lateinit var mockSoftwareKeyPair: JWSKeyPair

    @MockK
    private lateinit var mockKeyAttestation: KeyAttestation

    @MockK
    private lateinit var mockKeyAttestationJwt: Jwt

    private lateinit var useCase: GenerateDPoPKeyPair

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = GenerateDPoPKeyPairImpl(
            environmentSetupRepository = mockEnvironmentSetupRepository,
            requestKeyAttestation = mockRequestKeyAttestation,
            createJWSKeyPairInSoftware = mockCreateJWSKeyPairInSoftware,
        )

        every { mockEnvironmentSetupRepository.isDPopEnabled } returns true

        every { mockVerifiableCredentialParams.dpopSigningAlgValuesSupported } returns listOf(SigningAlgorithm.ES256)
        every { mockVerifiableCredentialParams.proofTypeConfig } returns ProofTypeConfig(
            proofSigningAlgValuesSupported = listOf(SigningAlgorithm.ES256),
            keyAttestationsRequired = KeyAttestationConfig(),
        )

        every { mockKeyAttestation.keyPair } returns mockHardwareKeyPair
        every { mockKeyAttestation.attestation } returns mockKeyAttestationJwt

        coEvery {
            mockRequestKeyAttestation(null, SigningAlgorithm.ES256)
        } returns Ok(mockKeyAttestation)

        coEvery { mockCreateJWSKeyPairInSoftware(SigningAlgorithm.ES256) } returns Ok(mockSoftwareKeyPair)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Generating dpop key pair succeeds`() = runTest {
        val result = useCase(mockVerifiableCredentialParams).assertOk()

        assertNotNull(result)
        assertEquals(mockHardwareKeyPair, result.keyPair)
        assertEquals(mockKeyAttestationJwt, result.attestationJwt)

        coVerify(exactly = 1) {
            mockRequestKeyAttestation(keyAlias = null, signingAlgorithm = SigningAlgorithm.ES256)
        }
        coVerify(exactly = 0) {
            mockCreateJWSKeyPairInSoftware(any())
        }
    }

    @Test
    fun `Generating software bound dpop key pair succeeds`() = runTest {
        every { mockVerifiableCredentialParams.proofTypeConfig } returns null

        val result = useCase(mockVerifiableCredentialParams).assertOk()

        assertNotNull(result)
        assertEquals(mockSoftwareKeyPair, result.keyPair)
        assertNull(result.attestationJwt)

        coVerify(exactly = 1) {
            mockCreateJWSKeyPairInSoftware(SigningAlgorithm.ES256)
        }
        coVerify(exactly = 0) {
            mockRequestKeyAttestation(any(), any())
        }
    }

    @Test
    fun `Generating dpop key pair where no signing alg values are supported returns null`() = runTest {
        every { mockVerifiableCredentialParams.dpopSigningAlgValuesSupported } returns emptyList()

        val result = useCase(mockVerifiableCredentialParams).assertOkNullable()

        assertNull(result)
    }

    @Test
    fun `Generating dpop key pair with disabled feature flag returns null`() = runTest {
        every { mockEnvironmentSetupRepository.isDPopEnabled } returns false

        val result = useCase(mockVerifiableCredentialParams).assertOkNullable()

        assertNull(result)
    }
}
