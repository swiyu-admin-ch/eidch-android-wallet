package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.JwkError
import ch.admin.foitt.openid4vc.domain.model.jwk.Jwk
import ch.admin.foitt.openid4vc.domain.usecase.CreateJwk
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockKeyPairs.UNSUPPORTED_KEY_PAIR
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockKeyPairs.VALID_KEY_PAIR_HARDWARE
import ch.admin.foitt.openid4vc.util.SafeJsonTestInstance
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CreateJwkImplTest {

    private val json = SafeJsonTestInstance.safeJson

    private lateinit var createJwk: CreateJwk

    @BeforeEach
    fun setUp() {
        createJwk = CreateJwkImpl()
    }

    @Test
    fun `creating a Jwk succeeds and return a valid Jwk`() = runTest {
        val keyPair = VALID_KEY_PAIR_HARDWARE

        val result = createJwk(
            algorithm = keyPair.algorithm,
            keyPair = keyPair.keyPair,
        ).assertOk()

        json.safeDecodeStringTo<Jwk>(result).assertOk()
    }

    @Test
    fun `creating a did jwk with an unsupported key pair should return an invalid cryptographic suite error`() = runTest {
        val keyPair = UNSUPPORTED_KEY_PAIR

        createJwk(
            algorithm = keyPair.algorithm,
            keyPair = keyPair.keyPair,
        ).assertErrorType(
            JwkError.UnsupportedCryptographicSuite::class
        )
    }
}
