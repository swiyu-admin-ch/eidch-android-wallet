package ch.admin.foitt.wallet.platform.keyPairGenerator.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.model.KeyPairError
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.usecase.CreateJWSKeyPairInSoftware
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class CreateJWSKeyPairInSoftwareImplTest {

    private lateinit var useCase: CreateJWSKeyPairInSoftware

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = CreateJWSKeyPairInSoftwareImpl()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Valid key pair returns success`() = runTest {
        val result = useCase(signingAlgorithm = SigningAlgorithm.ES256).assertOk()
        assertEquals(SigningAlgorithm.ES256, result.algorithm)
    }

    @Test
    fun `Exception during key pair generation returns an error`() = runTest {
        mockkStatic(UUID::class)
        every { UUID.randomUUID() } throws IllegalStateException()

        useCase(signingAlgorithm = SigningAlgorithm.ES256).assertErrorType(KeyPairError.Unexpected::class)
    }
}
