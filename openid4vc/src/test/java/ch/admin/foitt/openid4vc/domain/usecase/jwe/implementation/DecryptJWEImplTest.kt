package ch.admin.foitt.openid4vc.domain.usecase.jwe.implementation

import ch.admin.foitt.openid4vc.domain.model.jwe.JWEError
import ch.admin.foitt.openid4vc.domain.usecase.jwe.DecryptJWE
import ch.admin.foitt.openid4vc.util.assertErrorType
import io.mockk.MockKAnnotations
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.spec.ECGenParameterSpec

class DecryptJWEImplTest {

    private lateinit var decryptJWE: DecryptJWE

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        decryptJWE = DecryptJWEImpl()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    // Success tests are done in CreateJWEImplTest

    @Test
    fun `JWE with invalid format returns an error`() = runTest {
        val jwe = "invalid jwe"

        decryptJWE(
            jweString = jwe,
            privateKey = keyPair.private,
        ).assertErrorType(JWEError.Unexpected::class)
    }

    private val keyPair = createKeyPair()

    private fun createKeyPair(): KeyPair {
        val generator = KeyPairGenerator.getInstance("EC")
        val spec = ECGenParameterSpec("secp256r1")
        generator.initialize(spec)
        return generator.generateKeyPair()
    }
}
