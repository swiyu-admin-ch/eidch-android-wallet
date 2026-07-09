package ch.admin.foitt.wallet.platform.appAttestation

import ch.admin.foitt.didResolver.domain.DidResolverHelper
import ch.admin.foitt.openid4vc.domain.model.jwk.Jwk
import ch.admin.foitt.openid4vc.domain.model.jwk.hasSameCurveAs
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.jwt.JwtError
import ch.admin.foitt.openid4vc.domain.usecase.jwt.VerifyJwtSignatureFromDid
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AttestationError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestationResponse
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.ValidateClientAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.implementation.ValidateClientAttestationImpl
import ch.admin.foitt.wallet.platform.appAttestation.mock.ClientAttestationMocks
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.util.SafeJsonTestInstance
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.annotation.UnsafeResultValueAccess
import com.github.michaelbull.result.getOrThrow
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

@OptIn(UnsafeResultValueAccess::class)
class ValidateClientAttestationImplTest {
    @MockK
    private lateinit var mockDidResolverHelper: DidResolverHelper

    @MockK
    private lateinit var mockEnvironmentSetupRepository: EnvironmentSetupRepository

    @MockK
    private lateinit var mockVerifyJwtSignatureFromDid: VerifyJwtSignatureFromDid

    private var safeJson = spyk(SafeJsonTestInstance.safeJson)

    private val keyStoreAlias = "keyStoreAlias"
    private val jwk = Jwk.fromEcKey(ClientAttestationMocks.jwkEcP256_01, null).getOrThrow()
    private val issuerDid = "Did"
    private val keyId = "$issuerDid#key-01"
    private val clientAttestationRawJwt = ClientAttestationMocks.jwtAttestation01
    private val clientAttestationResponse = ClientAttestationResponse(clientAttestationRawJwt)

    private lateinit var useCase: ValidateClientAttestation

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        mockkConstructor(Jwt::class)
        mockkConstructor(Jwk::class)

        useCase = ValidateClientAttestationImpl(
            didResolverHelper = mockDidResolverHelper,
            environmentSetupRepo = mockEnvironmentSetupRepository,
            verifyJwtSignatureFromDid = mockVerifyJwtSignatureFromDid,
            safeJson = safeJson,
        )

        every { mockDidResolverHelper.getDidStringFromAbsoluteKeyId(keyId) } returns Ok(issuerDid)

        coEvery { anyConstructed<Jwt>().keyId } returns keyId
        coEvery { anyConstructed<Jwt>().expInstant } returns Instant.MAX
        coEvery { mockEnvironmentSetupRepository.attestationsServiceTrustedDids } returns listOf(issuerDid)
        coEvery { mockEnvironmentSetupRepository.appId } returns "swiyu"
        coEvery { mockVerifyJwtSignatureFromDid(any(), any()) } returns Ok(Unit)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `valid client attestation passes validation`() = runTest {
        val result = useCase(keyStoreAlias, jwk, clientAttestationResponse)
        val expectedClientAttestation = ClientAttestation(keyStoreAlias, Jwt(clientAttestationRawJwt))

        val clientAttestation: ClientAttestation = result.assertOk()

        assertEquals(expectedClientAttestation.attestation.rawJwt, clientAttestation.attestation.rawJwt)
    }

    @Test
    fun `invalid Jwt fails validation`() = runTest {
        val wrongJwt = "wrongJwt"

        useCase(keyStoreAlias, jwk, ClientAttestationResponse(wrongJwt))
            .assertErrorType(AttestationError.ValidationError::class)

        coVerify(exactly = 0) {
            anyConstructed<Jwt>().iss
            mockEnvironmentSetupRepository.attestationsServiceTrustedDids
            mockVerifyJwtSignatureFromDid(any(), any())
        }
    }

    @Test
    fun `missing kid fails validation`() = runTest {
        coEvery { anyConstructed<Jwt>().keyId } returns null
        useCase(keyStoreAlias, jwk, clientAttestationResponse).assertErrorType(AttestationError.ValidationError::class)

        coVerify(exactly = 1) {
            anyConstructed<Jwt>().keyId
        }

        coVerify(exactly = 0) {
            mockVerifyJwtSignatureFromDid(any(), any())
        }
    }

    @Test
    fun `validation maps errors from getting the issuer did`() = runTest {
        val exception = IllegalStateException("did error")
        every { mockDidResolverHelper.getDidStringFromAbsoluteKeyId(any()) } returns Err(exception)

        useCase(keyStoreAlias, jwk, clientAttestationResponse).assertErrorType(AttestationError.ValidationError::class)
    }

    @Test
    fun `unexpected kid fails validation`() = runTest {
        every { mockDidResolverHelper.getDidStringFromAbsoluteKeyId(keyId) } returns Ok("otherDid")

        useCase(keyStoreAlias, jwk, clientAttestationResponse).assertErrorType(AttestationError.ValidationError::class)

        coVerify(exactly = 0) {
            mockVerifyJwtSignatureFromDid(any(), any())
        }
    }

    @Test
    fun `unsupported attestation type fails validation`() = runTest {
        coEvery { anyConstructed<Jwt>().type } returns "unsupportedType"

        useCase(keyStoreAlias, jwk, clientAttestationResponse).assertErrorType(AttestationError.ValidationError::class)

        coVerify(exactly = 1) {
            anyConstructed<Jwt>().type
        }

        coVerify(exactly = 0) {
            mockVerifyJwtSignatureFromDid(any(), any())
        }
    }

    @Test
    fun `unsupported algorithm fails validation`() = runTest {
        coEvery { anyConstructed<Jwt>().algorithm } returns "unsupportedAlgorithm"

        useCase(keyStoreAlias, jwk, clientAttestationResponse).assertErrorType(AttestationError.ValidationError::class)

        coVerify(atLeast = 1) {
            anyConstructed<Jwt>().algorithm
        }

        coVerify(exactly = 0) {
            mockVerifyJwtSignatureFromDid(any(), any())
        }
    }

    @Test
    fun `failed signature verification fails validation`() = runTest {
        coEvery { mockVerifyJwtSignatureFromDid(any(), any()) } returns Err(JwtError.IssuerValidationFailed)

        useCase(keyStoreAlias, jwk, clientAttestationResponse).assertErrorType(AttestationError.ValidationError::class)

        coVerify(exactly = 1) {
            mockVerifyJwtSignatureFromDid(any(), any())
        }
    }

    @Test
    fun `missing exp fails validation`() = runTest {
        coEvery { anyConstructed<Jwt>().expInstant } returns null

        useCase(keyStoreAlias, jwk, clientAttestationResponse).assertErrorType(AttestationError.ValidationError::class)

        coVerify(exactly = 1) {
            anyConstructed<Jwt>().expInstant
        }
    }

    @Test
    fun `expired exp fails validation`() = runTest {
        coEvery { anyConstructed<Jwt>().expInstant } returns Instant.ofEpochSecond(0)

        useCase(keyStoreAlias, jwk, clientAttestationResponse).assertErrorType(AttestationError.ValidationError::class)

        coVerify(exactly = 1) {
            anyConstructed<Jwt>().expInstant
        }
    }

    @Test
    fun `missing nbf fails validation`() = runTest {
        coEvery { anyConstructed<Jwt>().nbfInstant } returns null

        useCase(keyStoreAlias, jwk, clientAttestationResponse).assertErrorType(AttestationError.ValidationError::class)

        coVerify(exactly = 1) {
            anyConstructed<Jwt>().nbfInstant
        }
    }

    @Test
    fun `missing wallet name fails validation`() = runTest {
        coEvery { anyConstructed<Jwt>().payloadJson } returns JsonObject(
            mapOf(
                "another_field" to JsonPrimitive("x"),
            )
        )

        useCase(keyStoreAlias, jwk, clientAttestationResponse).assertErrorType(AttestationError.ValidationError::class)

        coVerify(exactly = 1) {
            anyConstructed<Jwt>().payloadJson
        }
    }

    @Test
    fun `unsupported wallet name fails validation`() = runTest {
        coEvery { anyConstructed<Jwt>().payloadJson } returns JsonObject(
            mapOf(
                "wallet_name" to JsonPrimitive("anotherWallet"),
            )
        )

        useCase(keyStoreAlias, jwk, clientAttestationResponse).assertErrorType(AttestationError.ValidationError::class)

        coVerify(exactly = 1) {
            anyConstructed<Jwt>().payloadJson
        }
    }

    @Test
    fun `missing confirmation fails validation`() = runTest {
        coEvery { anyConstructed<Jwt>().payloadJson } returns JsonObject(
            mapOf(
                "wallet_name" to JsonPrimitive(mockEnvironmentSetupRepository.appId),
            )
        )

        useCase(keyStoreAlias, jwk, clientAttestationResponse).assertErrorType(AttestationError.ValidationError::class)

        coVerify(exactly = 2) {
            anyConstructed<Jwt>().payloadJson
        }
    }

    @Test
    fun `unsupported key in confirmation fails validation`() = runTest {
        val jwkObject = JsonObject(
            mapOf(
                "z" to JsonPrimitive("x"),
                "y" to JsonPrimitive("x"),
                "crv" to JsonPrimitive("x"),
                "kty" to JsonPrimitive("x"),
            )
        )
        coEvery { anyConstructed<Jwt>().payloadJson } returns JsonObject(
            mapOf(
                "wallet_name" to JsonPrimitive(mockEnvironmentSetupRepository.appId),
                "cnf" to JsonObject(
                    mapOf(
                        "jwk" to jwkObject
                    )
                )
            )
        )

        useCase(keyStoreAlias, jwk, clientAttestationResponse).assertErrorType(AttestationError.ValidationError::class)

        coVerify(exactly = 2) {
            anyConstructed<Jwt>().payloadJson
        }
    }

    @Test
    fun `key mismatch between confirmation and subject fails validation`() = runTest {
        coEvery { anyConstructed<Jwt>().subject } returns ClientAttestationMocks.jwkEcP256_02_didJwk

        useCase(keyStoreAlias, jwk, clientAttestationResponse).assertErrorType(AttestationError.ValidationError::class)

        coVerify(exactly = 2) {
            anyConstructed<Jwt>().payloadJson
        }
    }

    @Test
    fun `curve parameter mismatch between confirmation and original key fails validation`() = runTest {
        val otherJwk = safeJson.safeDecodeStringTo<Jwk>(ClientAttestationMocks.jwkEcP256_02).value
        mockkStatic(Jwk::hasSameCurveAs)

        useCase(keyStoreAlias, otherJwk, clientAttestationResponse).assertErrorType(AttestationError.ValidationError::class)

        coVerify(exactly = 1) {
            any<Jwk>().hasSameCurveAs(any())
        }
    }
}
