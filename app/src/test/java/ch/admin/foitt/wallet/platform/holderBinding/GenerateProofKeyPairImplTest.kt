package ch.admin.foitt.wallet.platform.holderBinding

import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.ProofTypeConfig
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBindingType
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AttestationError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.KeyAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.RequestKeyAttestation
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.proofTypeConfigHardwareBinding
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.proofTypeConfigSoftwareBinding
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.strongboxKeyStorage
import ch.admin.foitt.wallet.platform.holderBinding.domain.model.HolderBindingError
import ch.admin.foitt.wallet.platform.holderBinding.domain.usecase.GenerateProofKeyPairs
import ch.admin.foitt.wallet.platform.holderBinding.domain.usecase.implementation.GenerateProofKeyPairsImpl
import ch.admin.foitt.wallet.platform.holderBinding.mocks.KeyPairMocks.validKeyPairES256Hardware
import ch.admin.foitt.wallet.platform.holderBinding.mocks.KeyPairMocks.validKeyPairES256Software
import ch.admin.foitt.wallet.platform.holderBinding.mocks.KeyPairMocks.validKeyPairES512
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.model.KeyPairError
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.usecase.CreateJWSKeyPairInSoftware
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class GenerateProofKeyPairImplTest {

    @MockK
    private lateinit var mockRequestKeyAttestation: RequestKeyAttestation

    @MockK
    private lateinit var mockCreateJWSKeyPairInSoftware: CreateJWSKeyPairInSoftware

    @MockK
    private lateinit var mockKeyAttestationJwt: Jwt

    private lateinit var spyGenerateProofKeyPairs: GenerateProofKeyPairs

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        val generateKeyPair = GenerateProofKeyPairsImpl(
            createJWSKeyPairInSoftware = mockCreateJWSKeyPairInSoftware,
            requestKeyAttestation = mockRequestKeyAttestation,
        )
        spyGenerateProofKeyPairs = spyk(generateKeyPair)

        every { spyGenerateProofKeyPairs["getPreferredSigningAlgorithms"]() } returns listOf(
            SigningAlgorithm.ES256
        )
        coEvery {
            mockRequestKeyAttestation(null, SigningAlgorithm.ES256, any())
        } returns Ok(KeyAttestation(validKeyPairES256Hardware, mockKeyAttestationJwt))
        coEvery {
            mockRequestKeyAttestation(null, SigningAlgorithm.ES512, any())
        } returns Ok(KeyAttestation(validKeyPairES512, mockKeyAttestationJwt))
        coEvery { mockCreateJWSKeyPairInSoftware(SigningAlgorithm.ES256) } returns Ok(
            validKeyPairES256Software
        )
        coEvery { mockCreateJWSKeyPairInSoftware(SigningAlgorithm.ES512) } returns Ok(
            validKeyPairES512
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `valid supported credential with software binding returns key pair with software type`() = runTest {
        val result = spyGenerateProofKeyPairs(1, proofTypeConfigSoftwareBinding).assertOk().first()

        assertEquals(validKeyPairES256Software, result.keyPair)
        assertNull(result.attestationJwt)

        coVerify(exactly = 1) {
            mockCreateJWSKeyPairInSoftware(
                signingAlgorithm = SigningAlgorithm.ES256,
            )
        }
    }

    @Test
    fun `valid supported credential with hardware binding returns key pair`() = runTest {
        val result = spyGenerateProofKeyPairs(1, proofTypeConfigHardwareBinding).assertOk().first()

        assertEquals(validKeyPairES256Hardware, result.keyPair)
        assertEquals(mockKeyAttestationJwt, result.attestationJwt)

        coVerify(exactly = 1) {
            mockRequestKeyAttestation(
                keyAlias = null,
                signingAlgorithm = SigningAlgorithm.ES256,
                keyStorageSecurityLevels = strongboxKeyStorage,
            )
        }
    }

    @ParameterizedTest
    @MethodSource("generateSigningAlgorithmInputs")
    fun `Supported proof type with multiple algorithms returns a key pair using the first matching algorithm of the preference list`(
        issuerAlgorithms: List<SigningAlgorithm>,
        appAlgorithms: List<SigningAlgorithm>,
        expected: SigningAlgorithm,
    ) = runTest {
        every {
            spyGenerateProofKeyPairs["getPreferredSigningAlgorithms"]()
        } returns appAlgorithms

        val result = spyGenerateProofKeyPairs(1, ProofTypeConfig(issuerAlgorithms)).assertOk().first()

        assertEquals(expected, result.keyPair.algorithm)
        assertEquals(KeyBindingType.SOFTWARE, result.keyPair.bindingType)

        coVerify(exactly = 1) {
            mockCreateJWSKeyPairInSoftware(expected)
        }
    }

    @Test
    fun `Supported proof type with supported algorithm that is not in the preference list returns an unsupported cryptographic suite error`() =
        runTest {
            // issuer: proofType: JWT, algorithms: [ES256],
            // app: [ES512]
            // -> error
            every {
                spyGenerateProofKeyPairs["getPreferredSigningAlgorithms"]()
            } returns listOf(SigningAlgorithm.ES512)

            spyGenerateProofKeyPairs(
                1,
                proofTypeConfigSoftwareBinding
            ).assertErrorType(HolderBindingError.UnsupportedCryptographicSuite::class)

            coVerify(exactly = 0) {
                mockRequestKeyAttestation(null, SigningAlgorithm.ES256, any())
            }
        }

    @Test
    fun `Generate key pair maps errors from createJWSKeyPair`() = runTest {
        val exception = IllegalStateException("error when creating key pair")
        coEvery {
            mockRequestKeyAttestation(any(), any(), any())
        } returns Err(AttestationError.Unexpected(exception))

        spyGenerateProofKeyPairs(1, proofTypeConfigHardwareBinding).assertErrorType(HolderBindingError.Unexpected::class)
    }

    @Test
    fun `Generate key pair maps errors from createJWSKeyPairInSoftware`() = runTest {
        val exception = IllegalStateException("error when creating key pair")
        coEvery {
            mockCreateJWSKeyPairInSoftware(any())
        } returns Err(KeyPairError.Unexpected(exception))

        spyGenerateProofKeyPairs(1, proofTypeConfigSoftwareBinding).assertErrorType(HolderBindingError.Unexpected::class)
    }

    private companion object {
        @JvmStatic
        fun generateSigningAlgorithmInputs(): Stream<Arguments> = Stream.of(
            // Argument(issuer algorithms, app algorithms, expected result)
            Arguments.of(
                listOf(SigningAlgorithm.ES256, SigningAlgorithm.ES512),
                listOf(SigningAlgorithm.ES512, SigningAlgorithm.ES256),
                SigningAlgorithm.ES512
            ),
            Arguments.of(
                listOf(SigningAlgorithm.ES512, SigningAlgorithm.ES256),
                listOf(SigningAlgorithm.ES256, SigningAlgorithm.ES512),
                SigningAlgorithm.ES256
            ),
            Arguments.of(
                listOf(SigningAlgorithm.ES256, SigningAlgorithm.ES512),
                listOf(SigningAlgorithm.ES256),
                SigningAlgorithm.ES256
            ),
            Arguments.of(
                listOf(SigningAlgorithm.ES256, SigningAlgorithm.ES512),
                listOf(SigningAlgorithm.ES512),
                SigningAlgorithm.ES512
            )
        )
    }
}
