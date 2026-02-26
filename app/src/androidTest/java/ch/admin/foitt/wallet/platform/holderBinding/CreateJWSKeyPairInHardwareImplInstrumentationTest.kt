package ch.admin.foitt.wallet.platform.holderBinding

import android.content.Context
import android.content.pm.PackageManager
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import ch.admin.foitt.openid4vc.domain.model.KeyStorageSecurityLevel
import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.model.CreateKeyGenSpecError
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.model.KeyPairError
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.usecase.CreateJWSKeyPairInHardware
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.usecase.implementation.CreateJWSKeyPairInHardwareImpl
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.usecase.implementation.CreateKeyGenSpecImpl
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.util.UUID

class CreateJWSKeyPairInHardwareImplInstrumentationTest {

    private val testDispatcher = StandardTestDispatcher()

    @MockK
    private lateinit var mockAppContext: Context

    private val createKeyGenSpec = spyk(CreateKeyGenSpecImpl())

    private lateinit var useCase: CreateJWSKeyPairInHardware

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery {
            mockAppContext.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)
        } returns true

        useCase = CreateJWSKeyPairInHardwareImpl(
            appContext = mockAppContext,
            defaultDispatcher = testDispatcher,
            createKeyGenSpec = createKeyGenSpec,
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun validInputReturnsSuccess() = runTest(testDispatcher) {
        useCase(
            keyAlias = UUID.randomUUID().toString(),
            signingAlgorithm = SigningAlgorithm.ES256,
            provider = "AndroidKeyStore",
            keyStorageSecurityLevels = listOf(KeyStorageSecurityLevel.HIGH),
            attestationChallenge = null,
        ).assertOk()
    }

    @Test
    fun inputWithoutKeyAliasCreatesKeyIdReturnsSuccess() = runTest(testDispatcher) {
        mockkStatic(UUID::class)
        every { UUID.randomUUID().toString() } returns "UUID"

        useCase(
            keyAlias = null,
            signingAlgorithm = SigningAlgorithm.ES256,
            provider = "AndroidKeyStore",
            keyStorageSecurityLevels = listOf(KeyStorageSecurityLevel.HIGH),
            attestationChallenge = null,
        ).assertOk()

        coVerify {
            UUID.randomUUID().toString()
        }
    }

    @Test
    fun onKeyCollisionRetryReturnsSuccess() = runTest(testDispatcher) {
        mockkStatic(UUID::class)

        val provider = "AndroidKeyStore"
        val keyStore = KeyStore.getInstance(provider)
        keyStore.load(null)
        createMockKeyPair(keyAlias = "UUID1")

        every { UUID.randomUUID().toString() } returnsMany listOf("UUID1", "UUID2")

        val result = useCase(
            keyAlias = null,
            signingAlgorithm = SigningAlgorithm.ES256,
            provider = provider,
            keyStorageSecurityLevels = listOf(KeyStorageSecurityLevel.HIGH),
            attestationChallenge = null,
        ).assertOk()

        assertEquals("UUID2", result.keyId)
    }

    @Test
    fun onReachingKeyCollisionRetryLimitReturnsError() = runTest(testDispatcher) {
        mockkStatic(UUID::class)

        val provider = "AndroidKeyStore"
        val keyStore = KeyStore.getInstance(provider)
        keyStore.load(null)
        createMockKeyPair(keyAlias = "UUID1")
        createMockKeyPair(keyAlias = "UUID2")
        createMockKeyPair(keyAlias = "UUID3")
        createMockKeyPair(keyAlias = "UUID4")
        createMockKeyPair(keyAlias = "UUID5")

        every { UUID.randomUUID().toString() } returnsMany listOf("UUID1", "UUID2", "UUID3", "UUID4", "UUID5")

        useCase(
            keyAlias = null,
            signingAlgorithm = SigningAlgorithm.ES256,
            provider = provider,
            keyStorageSecurityLevels = listOf(KeyStorageSecurityLevel.HIGH),
            attestationChallenge = null,
        ).assertErrorType(KeyPairError.Unexpected::class)
    }

    @Test
    fun keyGenSpecHasTheCorrectStrongBoxFlag() = runTest(testDispatcher) {
        coEvery {
            mockAppContext.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)
        } returns true

        useCase(
            keyAlias = UUID.randomUUID().toString(),
            signingAlgorithm = SigningAlgorithm.ES256,
            provider = "AndroidKeyStore",
            keyStorageSecurityLevels = listOf(KeyStorageSecurityLevel.ENHANCED_BASIC, KeyStorageSecurityLevel.HIGH),
            attestationChallenge = null,
        ).assertOk()

        verify {
            createKeyGenSpec.invoke(any(), any(), true, any())
        }
    }

    @Test
    fun creatingTheKeyPairMapsErrorsFromCreatingTheKeyGenSpec() = runTest(testDispatcher) {
        val exception = IllegalStateException()
        coEvery {
            createKeyGenSpec(any(), any(), any(), any())
        } returns Err(KeyPairError.Unexpected(exception))

        useCase(
            keyAlias = UUID.randomUUID().toString(),
            signingAlgorithm = SigningAlgorithm.ES256,
            provider = "AndroidKeyStore",
            keyStorageSecurityLevels = listOf(KeyStorageSecurityLevel.HIGH),
            attestationChallenge = null,
        ).assertErrorType(KeyPairError.Unexpected::class)
    }

    private fun createMockKeyPair(
        keyAlias: String,
        signingAlgorithm: SigningAlgorithm = SigningAlgorithm.ES256,
    ) {
        val generator = KeyPairGenerator.getInstance(signingAlgorithm.toKeyAlgorithm(), "AndroidKeyStore")
        val spec = KeyGenParameterSpec.Builder(keyAlias, KeyProperties.PURPOSE_SIGN)
            .setDigests(signingAlgorithm.toDigest())
            .build()
        generator.initialize(spec)
        generator.generateKeyPair()
    }

    private fun SigningAlgorithm.toKeyAlgorithm() = when (this) {
        SigningAlgorithm.ES256,
        SigningAlgorithm.ES512 -> KeyProperties.KEY_ALGORITHM_EC
    }

    private fun SigningAlgorithm.toDigest() = when (this) {
        SigningAlgorithm.ES256 -> KeyProperties.DIGEST_SHA256
        SigningAlgorithm.ES512 -> KeyProperties.DIGEST_SHA512
    }
}
