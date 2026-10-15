package ch.admin.foitt.wallet.platform.keybindingMatching.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.jwk.Jwk
import ch.admin.foitt.openid4vc.domain.model.jwk.toEcJwk
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBindingType
import ch.admin.foitt.openid4vc.domain.model.toCurve
import ch.admin.foitt.openid4vc.domain.usecase.GetHardwareKeyPair
import ch.admin.foitt.wallet.platform.keybindingMatching.domain.model.MatchKeyBindingToPayloadCnfError
import ch.admin.foitt.wallet.platform.keybindingMatching.domain.usecase.MatchKeyBindingToPayloadCnf
import ch.admin.foitt.wallet.util.SafeJsonTestInstance
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Ok
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.X509EncodedKeySpec

class MatchKeyBindingToPayloadCnfImplTest {

    @MockK
    private lateinit var mockGetHardwareKeyPair: GetHardwareKeyPair

    private val safeJson = SafeJsonTestInstance.safeJson

    private lateinit var useCase: MatchKeyBindingToPayloadCnf

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = MatchKeyBindingToPayloadCnfImpl(
            safeJson = safeJson,
            getHardwareKeyPair = mockGetHardwareKeyPair,
        )
    }

    @Test
    fun `Returns matching KeyBinding for SOFTWARE binding when cnf jwk matches`() = runTest {
        val swKeyPair = generateEcKeyPair()
        val softwareKb = softwareKeyBinding(
            id = "sw1",
            publicKey = swKeyPair.public as ECPublicKey,
        )

        val otherKeyPair = generateEcKeyPair()
        val otherKb = softwareKeyBinding(
            id = "sw2",
            publicKey = otherKeyPair.public as ECPublicKey,
        )

        val cnfJwk = publicKeyToJwk(swKeyPair.public as ECPublicKey)
        val payload = buildJwtWithCnf(cnfJwk)

        val result = useCase(listOf(otherKb, softwareKb), payload).assertOk()
        assertEquals(softwareKb, result)
    }

    @Test
    fun `Returns matching KeyBinding for HARDWARE binding when cnf jwk matches`() = runTest {
        val hwKeyPair = generateEcKeyPair()
        val hardwareKb = KeyBinding(
            identifier = "hw1",
            algorithm = SigningAlgorithm.ES256,
            bindingType = KeyBindingType.HARDWARE,
        )

        coEvery { mockGetHardwareKeyPair("hw1", any()) } returns Ok(hwKeyPair)

        val cnfJwk = publicKeyToJwk(hwKeyPair.public as ECPublicKey)
        val payload = buildJwtWithCnf(cnfJwk)

        val result = useCase.invoke(listOf(hardwareKb), payload).assertOk()
        assertEquals(hardwareKb, result)
    }

    @Test
    fun `Returns error when no matching key binding is available`() = runTest {
        val keyPair = generateEcKeyPair()
        val nonMatchingKb = softwareKeyBinding(
            id = "swX",
            publicKey = keyPair.public as ECPublicKey,
        )

        val differentKeyPair = generateEcKeyPair()
        val cnfJwk = publicKeyToJwk(differentKeyPair.public as ECPublicKey)
        val payload = buildJwtWithCnf(cnfJwk)

        useCase(listOf(nonMatchingKb), payload).assertErrorType(MatchKeyBindingToPayloadCnfError.Unexpected::class)
    }

    @Test
    fun `Maps JSON parsing error to Unexpected when cnf jwk is invalid`() = runTest {
        val swKeyPair = generateEcKeyPair()
        val softwareKb = softwareKeyBinding(
            id = "swInvalid",
            publicKey = swKeyPair.public as ECPublicKey,
        )

        // Create a payload with malformed jwk (missing required fields)
        val payload = buildJwtWithRawCnf(mapOf("jwk" to emptyMap<String, Any>()))

        useCase(listOf(softwareKb), payload).assertErrorType(MatchKeyBindingToPayloadCnfError.Unexpected::class)
    }

    // Helpers
    private fun softwareKeyBinding(id: String, algorithm: SigningAlgorithm = SigningAlgorithm.ES256, publicKey: ECPublicKey): KeyBinding {
        val factory = KeyFactory.getInstance("EC")
        val encoded = publicKey.encoded
        // Decode to ensure format is correct as expected by implementation
        val restored = factory.generatePublic(X509EncodedKeySpec(encoded)) as ECPublicKey
        return KeyBinding(
            identifier = id,
            algorithm = algorithm,
            bindingType = KeyBindingType.SOFTWARE,
            publicKey = restored.encoded,
        )
    }

    private fun generateEcKeyPair(algorithm: SigningAlgorithm = SigningAlgorithm.ES256): KeyPair {
        val kpg = KeyPairGenerator.getInstance("EC")
        val curveName = when (algorithm) {
            SigningAlgorithm.ES256 -> "secp256r1"
            SigningAlgorithm.ES512 -> "secp521r1"
        }
        kpg.initialize(ECGenParameterSpec(curveName))
        return kpg.generateKeyPair()
    }

    private fun publicKeyToJwk(publicKey: ECPublicKey, algorithm: SigningAlgorithm = SigningAlgorithm.ES256): Jwk {
        val ecKey = ECKey.Builder(algorithm.toCurve(), publicKey).build()
        return ecKey.toEcJwk(certificateChainBase64 = null)
    }

    private fun buildJwtWithCnf(jwk: Jwk): String {
        val cnfMap = mapOf(
            "jwk" to mapOf(
                "x" to jwk.x,
                "y" to jwk.y,
                "crv" to jwk.crv,
                "kty" to jwk.kty,
            )
        )
        return buildJwtWithRawCnf(cnfMap)
    }

    private fun buildJwtWithRawCnf(cnf: Map<String, Any>): String {
        val claims = JWTClaimsSet.Builder()
            .claim("cnf", cnf)
            .build()

        val header = JWSHeader.Builder(JWSAlgorithm.HS256).build()
        val signed = SignedJWT(header, claims)
        val signer = MACSigner(ByteArray(32) { 1 })
        signed.sign(signer)
        return signed.serialize() + "~"
    }
}
