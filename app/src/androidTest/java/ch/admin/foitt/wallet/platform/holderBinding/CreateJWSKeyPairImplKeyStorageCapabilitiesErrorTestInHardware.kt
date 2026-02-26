package ch.admin.foitt.wallet.platform.holderBinding

import android.content.Context
import android.content.pm.PackageManager
import ch.admin.foitt.openid4vc.domain.model.KeyStorageSecurityLevel
import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.model.KeyPairError
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.usecase.CreateJWSKeyPairInHardware
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.usecase.implementation.CreateJWSKeyPairInHardwareImpl
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.usecase.implementation.CreateKeyGenSpecImpl
import ch.admin.foitt.wallet.util.assertErrorType
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.UUID

@RunWith(Parameterized::class)
class CreateJWSKeyPairImplKeyStorageCapabilitiesErrorTestInHardware(
    private val keyStorageList: List<KeyStorageSecurityLevel>,
    private val deviceHasStrongBox: Boolean,
) {

    private val testDispatcher = StandardTestDispatcher()

    @MockK
    private lateinit var mockAppContext: Context

    private val keyGenSpecFactory = spyk(CreateKeyGenSpecImpl())

    private lateinit var useCase: CreateJWSKeyPairInHardware

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = CreateJWSKeyPairInHardwareImpl(
            appContext = mockAppContext,
            defaultDispatcher = testDispatcher,
            createKeyGenSpec = keyGenSpecFactory,
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun keyGenSpecHasTheCorrectStrongBoxFlag() = runTest(testDispatcher) {
        coEvery {
            mockAppContext.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)
        } returns deviceHasStrongBox

        useCase(
            keyAlias = UUID.randomUUID().toString(),
            signingAlgorithm = SigningAlgorithm.ES256,
            provider = "AndroidKeyStore",
            keyStorageSecurityLevels = keyStorageList,
            attestationChallenge = null,
        ).assertErrorType(KeyPairError.IncompatibleDeviceProofKeyStorage::class)

        verify(exactly = 0) {
            keyGenSpecFactory.invoke(any(), any(), any(), any())
        }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): List<Array<Any>> {
            return listOf(
                // key_storage list, deviceHasStrongBox
                arrayOf(listOf(KeyStorageSecurityLevel.HIGH), false),
                arrayOf(listOf(KeyStorageSecurityLevel.MODERATE, KeyStorageSecurityLevel.HIGH), false),
            )
        }
    }
}
