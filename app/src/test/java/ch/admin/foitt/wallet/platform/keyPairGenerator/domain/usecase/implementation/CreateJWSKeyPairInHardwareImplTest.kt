package ch.admin.foitt.wallet.platform.keyPairGenerator.domain.usecase.implementation

import android.content.Context
import android.content.pm.PackageManager
import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.usecase.CreateJWSKeyPairInHardware
import ch.admin.foitt.wallet.util.assertErr
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CreateJWSKeyPairInHardwareImplTest {

    private val testDispatcher = StandardTestDispatcher()

    @MockK
    private lateinit var mockAppContext: Context

    private lateinit var useCase: CreateJWSKeyPairInHardware

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery {
            mockAppContext.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)
        } returns true

        useCase = CreateJWSKeyPairInHardwareImpl(
            appContext = mockAppContext,
            defaultDispatcher = testDispatcher,
            createKeyGenSpec = CreateKeyGenSpecImpl(),
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `return error if using non-existing KeyStore Provider`() = runTest(testDispatcher) {
        useCase(
            signingAlgorithm = SigningAlgorithm.ES256,
            provider = "this provider does not exist",
            attestationChallenge = null,
        ).assertErr()
    }
}
