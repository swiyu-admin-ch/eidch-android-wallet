package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.JwkError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.keyBinding.BindingKeyPair
import ch.admin.foitt.openid4vc.domain.usecase.CreateJwk
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.CREDENTIAL_ISSUER
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.C_NONCE
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockKeyPairs.INVALID_KEY_PAIR
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockKeyPairs.VALID_KEY_PAIR_HARDWARE
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.get
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jwt.SignedJWT
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.security.interfaces.ECPublicKey

class CreateCredentialRequestProofsJwtImplTest {

    private val testDispatcher = StandardTestDispatcher()

    @MockK
    private lateinit var mockCreateJwk: CreateJwk

    @MockK
    private lateinit var mockKeyAttestationJwt: Jwt

    private lateinit var createCredentialRequestProofsJwtUseCase: CreateCredentialRequestProofsJwtImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery {
            mockCreateJwk(any(), any(), false)
        } returns Ok(jwk)

        every { mockKeyAttestationJwt.rawJwt } returns MockCredentialOffer.KEY_ATTESTATION_JWT

        createCredentialRequestProofsJwtUseCase = CreateCredentialRequestProofsJwtImpl(createJwk = mockCreateJwk)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `a created proof jwt should have a valid signature`() = runTest(testDispatcher) {
        val keyPair = VALID_KEY_PAIR_HARDWARE
        val proofJwt = createCredentialRequestProofsJwtUseCase(
            keyPairs = listOf(BindingKeyPair(keyPair, null)),
            issuer = CREDENTIAL_ISSUER.toString(),
            cNonce = C_NONCE,
        )
        proofJwt.assertOk()
        val jwt = proofJwt.get()?.jwt?.first()
        val publicKey = keyPair.keyPair.public as ECPublicKey
        val verifier = ECDSAVerifier(publicKey)
        assertTrue(SignedJWT.parse(jwt).verify(verifier), "")
    }

    @Test
    fun `a created proof jwt without nonce should have a valid signature`() = runTest(testDispatcher) {
        val keyPair = VALID_KEY_PAIR_HARDWARE
        val proofJwt = createCredentialRequestProofsJwtUseCase(
            keyPairs = listOf(BindingKeyPair(keyPair, null)),
            issuer = CREDENTIAL_ISSUER.toString(),
            cNonce = C_NONCE,
        )
        proofJwt.assertOk()
        val jwt = proofJwt.get()?.jwt?.first()
        val publicKey = keyPair.keyPair.public as ECPublicKey
        val verifier = ECDSAVerifier(publicKey)
        assertTrue(SignedJWT.parse(jwt).verify(verifier), "")
    }

    @Test
    fun `a created proof jwt with a key attestation jwt should have a valid signature and contain the attestation jwt as header`() =
        runTest(testDispatcher) {
            val keyPair = VALID_KEY_PAIR_HARDWARE
            val proofJwt = createCredentialRequestProofsJwtUseCase(
                keyPairs = listOf(BindingKeyPair(keyPair, mockKeyAttestationJwt)),
                issuer = CREDENTIAL_ISSUER.toString(),
                cNonce = null,
            ).assertOk()

            val jwt = proofJwt.jwt.first()
            val publicKey = keyPair.keyPair.public as ECPublicKey
            val verifier = ECDSAVerifier(publicKey)
            val signedJwt = SignedJWT.parse(jwt)
            assertTrue(signedJwt.verify(verifier))
            assertEquals(
                MockCredentialOffer.KEY_ATTESTATION_JWT,
                signedJwt.header.getCustomParam("key_attestation")
            )
        }

    @Test
    fun `creating a proof jwt with an invalid private key should return an unexpected error`() = runTest(testDispatcher) {
        val keyPair = INVALID_KEY_PAIR
        val proofJwt = createCredentialRequestProofsJwtUseCase(
            keyPairs = listOf(BindingKeyPair(keyPair, null)),
            issuer = CREDENTIAL_ISSUER.toString(),
            cNonce = C_NONCE,
        )

        proofJwt.assertErrorType(CredentialOfferError.Unexpected::class)
    }

    @Test
    fun `should return an unexpected error when header jwk creation fails`() = runTest(testDispatcher) {
        coEvery {
            mockCreateJwk(any(), any(), false)
        } returns Err(JwkError.Unexpected(null))

        val proofJwt = createCredentialRequestProofsJwtUseCase(
            keyPairs = listOf(BindingKeyPair(VALID_KEY_PAIR_HARDWARE, null)),
            issuer = CREDENTIAL_ISSUER.toString(),
            cNonce = C_NONCE,
        )

        proofJwt.assertErrorType(CredentialOfferError.Unexpected::class)
    }

    private val jwk = """
    {
        "crv": "P-256",
        "kty": "EC",
        "x": "Q7HpY9d8GlvGqfHtw-9jLLPZaIX9Lc91Q-Hfsz_WbBo",
        "y": "647ttGFFCBoy17NspJszfIW2pEwuzqdep69Av5Mprb8"
    }
    """
}
