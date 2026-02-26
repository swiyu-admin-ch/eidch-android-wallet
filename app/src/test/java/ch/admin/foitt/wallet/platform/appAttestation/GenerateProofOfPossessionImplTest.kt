package ch.admin.foitt.wallet.platform.appAttestation

import ch.admin.foitt.openid4vc.domain.model.KeyPairError
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.usecase.GetHardwareKeyPair
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AttestationError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.implementation.GenerateProofOfPossessionImpl
import ch.admin.foitt.wallet.util.SafeJsonTestInstance
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.annotation.UnsafeResultValueAccess
import com.nimbusds.jwt.SignedJWT
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonElement
import org.erdtman.jcs.JsonCanonicalizer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.spec.ECGenParameterSpec

class GenerateProofOfPossessionImplTest {
    @MockK
    private lateinit var mockGetHardwareKeyPair: GetHardwareKeyPair

    private val testSafeJson = SafeJsonTestInstance.safeJson

    private val testAlias = "testAlias"

    private val testKeyPair: KeyPair by lazy {
        val generator = KeyPairGenerator.getInstance("EC")
        generator.initialize(ECGenParameterSpec("secp521r1"), SecureRandom())
        generator.generateKeyPair()
    }

    @MockK
    private lateinit var mockClientAttestation: ClientAttestation

    @MockK
    private lateinit var mockJwt: Jwt

    private val testChallenge = "testChallenge"
    private val testAudience = "testAudience"

    @OptIn(UnsafeResultValueAccess::class)
    private val testRequestBody = testSafeJson.safeDecodeStringTo<JsonElement>(
        """
        {
            "testKey": "testValue 1",
            "testKey2": 123,
            "testKey3": true
        }
        """.trimIndent()
    ).value

    private lateinit var useCase: GenerateProofOfPossessionImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = GenerateProofOfPossessionImpl(
            getKeyPair = mockGetHardwareKeyPair
        )

        coEvery { mockGetHardwareKeyPair.invoke(keyId = any(), provider = any()) } returns Ok(testKeyPair)
        coEvery { mockClientAttestation.keyStoreAlias } returns testAlias
        coEvery { mockClientAttestation.attestation } returns mockJwt
        coEvery { mockJwt.subject } returns "testSubject"
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `A success returns a keyPair and follows specific steps`() = runTest {
        val result = useCase(
            clientAttestation = mockClientAttestation,
            challenge = testChallenge,
            audience = testAudience,
            requestBody = testRequestBody,
        )

        result.assertOk()

        coVerifyOrder {
            mockGetHardwareKeyPair.invoke(keyId = testAlias, provider = any())
        }
    }

    @Test
    fun `A canonicalization failure is propagated`() = runTest {
        mockkConstructor(JsonCanonicalizer::class)

        val exception = Exception("my Exception")

        coEvery { anyConstructed<JsonCanonicalizer>().encodedUTF8 } throws exception

        val result = useCase(
            clientAttestation = mockClientAttestation,
            challenge = testChallenge,
            audience = testAudience,
            requestBody = testRequestBody,
        )

        val error = result.assertErrorType(AttestationError.Unexpected::class)

        assertEquals(exception, error.throwable)
    }

    @Test
    fun `A getKeyPair failure is propagated`() = runTest {
        val exception = Exception("my Exception")

        coEvery {
            mockGetHardwareKeyPair.invoke(keyId = any(), provider = any())
        } returns Err(KeyPairError.Unexpected(exception))

        val result = useCase(
            clientAttestation = mockClientAttestation,
            challenge = testChallenge,
            audience = testAudience,
            requestBody = testRequestBody,
        )

        val error = result.assertErrorType(AttestationError.Unexpected::class)

        assertEquals(exception, error.throwable)
    }

    @Test
    fun `A jwt signing failure is propagated`() = runTest {
        mockkConstructor(SignedJWT::class)
        val exception = Exception("my Exception")
        coEvery { anyConstructed<SignedJWT>().sign(any()) } throws exception

        val result = useCase(
            clientAttestation = mockClientAttestation,
            challenge = testChallenge,
            audience = testAudience,
            requestBody = testRequestBody,
        )

        val error = result.assertErrorType(AttestationError.Unexpected::class)

        assertEquals(exception, error.throwable)
    }

    @Test
    fun `A jwt serialization failure is propagated`() = runTest {
        mockkConstructor(SignedJWT::class)
        val exception = Exception("my Exception")
        coEvery { anyConstructed<SignedJWT>().serialize() } throws exception

        val result = useCase(
            clientAttestation = mockClientAttestation,
            challenge = testChallenge,
            audience = testAudience,
            requestBody = testRequestBody,
        )

        val error = result.assertErrorType(AttestationError.Unexpected::class)

        assertEquals(exception, error.throwable)
    }
}
