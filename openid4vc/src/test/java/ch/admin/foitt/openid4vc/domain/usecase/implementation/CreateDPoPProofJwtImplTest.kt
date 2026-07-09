package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.JwkError
import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.JWSKeyPair
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBindingType
import ch.admin.foitt.openid4vc.domain.usecase.CreateJwk
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import ch.admin.foitt.openid4vc.utils.Constants
import ch.admin.foitt.openid4vc.utils.toBase64StringUrlEncodedWithoutPadding
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jwt.SignedJWT
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec

class CreateDPoPProofJwtImplTest {

    @MockK
    private lateinit var mockCreateJwk: CreateJwk

    @MockK
    private lateinit var mockKeyAttestationJwt: Jwt

    private lateinit var createDPoPProofJwt: CreateDPoPProofJwtImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery {
            mockCreateJwk(any(), any())
        } returns Ok(jwk)

        every { mockKeyAttestationJwt.rawJwt } returns KEY_ATTESTATION_JWT

        createDPoPProofJwt = CreateDPoPProofJwtImpl(
            createJwk = mockCreateJwk,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `creating a dpop proof jwt returns a signed proof with expected header and claims`() = runTest {
        val proof = createDPoPProofJwt(
            method = "post",
            url = URL("https://issuer.example/token?ignored=true"),
            keyPair = validKeyPair,
            nonce = NONCE,
            accessToken = ACCESS_TOKEN,
            keyAttestationJwt = mockKeyAttestationJwt,
        ).assertOk()

        val signedJwt = SignedJWT.parse(proof)
        val verifier = ECDSAVerifier(validKeyPair.keyPair.public as ECPublicKey)

        assertTrue(signedJwt.verify(verifier))
        assertEquals(Constants.DPOP_JWT_PROOF_HEADER_TYPE, signedJwt.header.type.type)
        assertEquals(Constants.DPOP_SWISS_PROFILE_VERSION, signedJwt.header.getCustomParam(Constants.DPOP_SWISS_PROFILE_HEADER))
        assertEquals(KEY_ATTESTATION_JWT, signedJwt.header.getCustomParam("key_attestation"))
        assertEquals("POST", signedJwt.jwtClaimsSet.getStringClaim("htm"))
        assertEquals("https://issuer.example/token", signedJwt.jwtClaimsSet.getStringClaim("htu"))
        assertEquals(NONCE, signedJwt.jwtClaimsSet.getStringClaim("nonce"))
        assertEquals(expectedAth(ACCESS_TOKEN), signedJwt.jwtClaimsSet.getStringClaim("ath"))
    }

    @Test
    fun `creating a dpop proof jwt without nonce or access token omits optional claims`() = runTest {
        val proof = createDPoPProofJwt(
            method = "get",
            url = URL("https://issuer.example/credential"),
            keyPair = validKeyPair,
            nonce = null,
            accessToken = null,
            keyAttestationJwt = null,
        ).assertOk()

        val signedJwt = SignedJWT.parse(proof)

        assertEquals("GET", signedJwt.jwtClaimsSet.getStringClaim("htm"))
        assertEquals("https://issuer.example/credential", signedJwt.jwtClaimsSet.getStringClaim("htu"))
        assertEquals(null, signedJwt.jwtClaimsSet.getClaim("nonce"))
        assertEquals(null, signedJwt.jwtClaimsSet.getClaim("ath"))
        assertEquals(null, signedJwt.header.getCustomParam("key_attestation"))
    }

    @Test
    fun `creating a dpop proof jwt maps jwk creation errors`() = runTest {
        coEvery {
            mockCreateJwk(any(), any())
        } returns Err(JwkError.Unexpected(null))

        createDPoPProofJwt(
            method = "post",
            url = URL("https://issuer.example/token"),
            keyPair = validKeyPair,
            nonce = NONCE,
            accessToken = ACCESS_TOKEN,
            keyAttestationJwt = null,
        ).assertErrorType(CredentialOfferError.Unexpected::class)
    }

    private companion object {
        private const val NONCE = "nonce"
        private const val ACCESS_TOKEN = "access-token"
        private const val KEY_ATTESTATION_JWT = "key-attestation-jwt"

        private val validKeyPair = createEs256KeyPair()

        private const val jwk = """
        {
            "crv": "P-256",
            "kty": "EC",
            "x": "Q7HpY9d8GlvGqfHtw-9jLLPZaIX9Lc91Q-Hfsz_WbBo",
            "y": "647ttGFFCBoy17NspJszfIW2pEwuzqdep69Av5Mprb8"
        }
        """

        private fun createEs256KeyPair(): JWSKeyPair {
            val generator = KeyPairGenerator.getInstance("EC")
            generator.initialize(ECGenParameterSpec("secp256r1"), SecureRandom())
            return JWSKeyPair(
                algorithm = SigningAlgorithm.ES256,
                keyPair = generator.generateKeyPair(),
                keyId = "key-id",
                bindingType = KeyBindingType.SOFTWARE,
            )
        }

        private fun expectedAth(accessToken: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            return digest.digest(accessToken.toByteArray(Charsets.US_ASCII))
                .toBase64StringUrlEncodedWithoutPadding() + "="
        }
    }
}
