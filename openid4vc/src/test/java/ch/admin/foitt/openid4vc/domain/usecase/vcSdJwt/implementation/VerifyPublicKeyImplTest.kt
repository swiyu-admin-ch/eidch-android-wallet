package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation

import ch.admin.eid.didresolver.did_sidekicks.Jwk
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.VerifyPublicKey
import ch.admin.foitt.openid4vc.util.assertErr
import ch.admin.foitt.openid4vc.util.assertOk
import com.nimbusds.jwt.SignedJWT
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VerifyPublicKeyImplTest {

    @MockK
    private lateinit var mockPublicKey: Jwk

    @MockK
    private lateinit var mockSignedJwt: SignedJWT

    private lateinit var useCase: VerifyPublicKey

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        every { mockPublicKey.crv } returns CURVE
        every { mockPublicKey.x } returns X_VALUE
        every { mockPublicKey.y } returns Y_VALUE
        every { mockSignedJwt.verify(any()) } returns true

        useCase = VerifyPublicKeyImpl()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Successfully verifying a public key returns Ok`() = runTest {
        val result = useCase(mockPublicKey, mockSignedJwt)
        result.assertOk()
    }

    @Test
    fun `Verifying a signature that does not match returns an error`() = runTest {
        every { mockSignedJwt.verify(any()) } returns false

        val result = useCase(mockPublicKey, mockSignedJwt)
        result.assertErr()
    }

    @Test
    fun `Error during curve creation returns an error`() = runTest {
        every { mockPublicKey.crv } returns "something"

        val result = useCase(mockPublicKey, mockSignedJwt)
        result.assertErr()
    }

    @Test
    fun `Error during key creation returns an error`() = runTest {
        every { mockPublicKey.x } returns "xValue"
        every { mockPublicKey.y } returns "yValue"

        val result = useCase(mockPublicKey, mockSignedJwt)
        result.assertErr()
    }

    private companion object {
        const val CURVE = "P-256"
        const val X_VALUE = "_AJp5rIScnVgfu7QPOPYb3dAX9qdUjZ4BDWlIuaQhmA"
        const val Y_VALUE = "RMk5JZx7riq5r54j96Mtje4NSR1tjP4XhedswL2MQfs"
    }
}
