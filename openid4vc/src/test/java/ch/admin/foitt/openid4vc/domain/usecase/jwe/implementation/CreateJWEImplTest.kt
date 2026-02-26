package ch.admin.foitt.openid4vc.domain.usecase.jwe.implementation

import ch.admin.foitt.openid4vc.domain.model.jwe.JWEError
import ch.admin.foitt.openid4vc.domain.model.jwk.Jwk
import ch.admin.foitt.openid4vc.domain.usecase.jwe.CreateJWE
import ch.admin.foitt.openid4vc.domain.usecase.jwe.DecryptJWE
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import com.nimbusds.jose.jwk.Curve.P_256
import com.nimbusds.jose.jwk.ECKey
import io.mockk.MockKAnnotations
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec

class CreateJWEImplTest {

    private lateinit var createJWE: CreateJWE
    private lateinit var decryptJWE: DecryptJWE

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        createJWE = CreateJWEImpl()
        decryptJWE = DecryptJWEImpl()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `JWE with compression created with public key can be decrypted with private key`() = runTest {
        val payload = "payload"

        val jwe = createJWE(
            algorithm = ALG_VALUE,
            encryptionMethod = ENCRYPTION_VALUE,
            compressionAlgorithm = ZIP_VALUE,
            payload = payload,
            encryptionKey = publicKeyJwk,
        ).assertOk()

        val decryptedPayload = decryptJWE(
            jweString = jwe,
            privateKey = keyPair.private,
        ).assertOk()

        assertEquals(payload, decryptedPayload)
    }

    @Test
    fun `JWE without compression created with public key can be decrypted with private key`() = runTest {
        val payload = "payload"

        val jwe = createJWE(
            algorithm = ALG_VALUE,
            encryptionMethod = ENCRYPTION_VALUE,
            compressionAlgorithm = null,
            payload = payload,
            encryptionKey = publicKeyJwk,
        ).assertOk()

        val decryptedPayload = decryptJWE(
            jweString = jwe,
            privateKey = keyPair.private,
        ).assertOk()

        assertEquals(payload, decryptedPayload)
    }

    @Test
    fun `Invalid algorithm returns error`() = runTest {
        createJWE(
            algorithm = "invalid algorithm",
            encryptionMethod = ENCRYPTION_VALUE,
            compressionAlgorithm = ZIP_VALUE,
            payload = "payload",
            encryptionKey = publicKeyJwk,
        ).assertErrorType(JWEError.Unexpected::class)
    }

    @Test
    fun `Invalid encryption method returns error`() = runTest {
        createJWE(
            algorithm = ALG_VALUE,
            encryptionMethod = "invalid encryption",
            compressionAlgorithm = ZIP_VALUE,
            payload = "payload",
            encryptionKey = publicKeyJwk,
        ).assertErrorType(JWEError.Unexpected::class)
    }

    private val keyPair = createKeyPair()
    val publicKey: ECKey = ECKey.Builder(P_256, keyPair.public as ECPublicKey).build()
    val publicKeyJwk = Jwk(
        x = publicKey.x.toString(),
        y = publicKey.y.toString(),
        crv = publicKey.curve.name,
        kty = publicKey.keyType.value,
        kid = publicKey.keyID,
    )

    private fun createKeyPair(): KeyPair {
        val generator = KeyPairGenerator.getInstance("EC")
        val spec = ECGenParameterSpec("secp256r1")
        generator.initialize(spec)
        return generator.generateKeyPair()
    }

    private companion object {
        const val ALG_VALUE = "ECDH-ES"
        const val ENCRYPTION_VALUE = "A128GCM"
        const val ZIP_VALUE = "DEF"
    }
}
