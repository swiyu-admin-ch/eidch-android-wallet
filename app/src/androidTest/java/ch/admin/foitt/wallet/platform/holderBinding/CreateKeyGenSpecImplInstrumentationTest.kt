package ch.admin.foitt.wallet.platform.holderBinding

import android.security.keystore.KeyProperties
import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.usecase.CreateKeyGenSpec
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.usecase.implementation.CreateKeyGenSpecImpl
import ch.admin.foitt.wallet.util.assertOk
import io.mockk.MockKAnnotations
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

class CreateKeyGenSpecImplInstrumentationTest {

    private lateinit var useCase: CreateKeyGenSpec

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = CreateKeyGenSpecImpl()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun validInputReturnsKeyGenSpec() = runTest {
        val result = useCase(
            keyId = KEY_ID,
            signingAlgorithm = SigningAlgorithm.ES256,
            useStrongBox = USE_STRONGBOX,
            attestationChallenge = attestationChallenge,
        ).assertOk()

        assertEquals(KEY_ID, result.keystoreAlias)
        assertEquals(KeyProperties.DIGEST_SHA256, result.digests.first())
        assertEquals(USE_STRONGBOX, result.isStrongBoxBacked)
        assertTrue(attestationChallenge.contentEquals(result.attestationChallenge))
    }

    private companion object {
        const val KEY_ID = "keyId"
        const val USE_STRONGBOX = true
        val attestationChallenge = byteArrayOf(0, 1)
    }
}
