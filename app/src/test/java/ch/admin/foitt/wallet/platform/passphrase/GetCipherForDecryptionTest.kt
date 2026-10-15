package ch.admin.foitt.wallet.platform.passphrase

import android.annotation.SuppressLint
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.model.KeystoreError
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.model.KeystoreKeyConfig
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.usecase.GetCipherForDecryption
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.usecase.GetOrCreateSecretKey
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.usecase.implementation.GetCipherForDecryptionImpl
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.security.NoSuchAlgorithmException
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.SecretKey

class GetCipherForDecryptionTest {

    @MockK
    private lateinit var mockGetOrCreateSecretKey: GetOrCreateSecretKey

    @MockK
    private lateinit var mockCipher: Cipher

    @MockK
    private lateinit var mockSecretKey: SecretKey

    @MockK
    private lateinit var mockPassConfig: KeystoreKeyConfig

    private lateinit var useCase: GetCipherForDecryption

    private val initializationVector = byteArrayOf(1, 1, 1, 1)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        mockkStatic(Cipher::class)

        coEvery { mockGetOrCreateSecretKey(any()) } returns Ok(mockSecretKey)

        coEvery { mockSecretKey.format } returns "AES"

        coEvery { Cipher.getInstance(any()) } returns mockCipher

        coEvery { mockCipher.init(any(), any(), any<AlgorithmParameterSpec>()) } just runs

        coEvery { mockPassConfig.encryptionTransformation } returns "AES/GCM/NoPadding"
        coEvery { mockPassConfig.gcmAuthTagLength } returns 128

        useCase = GetCipherForDecryptionImpl(
            getOrCreateSecretKey = mockGetOrCreateSecretKey,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @SuppressLint("CheckResult")
    @Test
    fun `Getting an initialized Cipher follow specific steps`() = runTest {
        useCase(
            keystoreKeyConfig = mockPassConfig,
            initializationVector = initializationVector
        ).assertOk()

        coVerify(exactly = 1) {
            mockGetOrCreateSecretKey(any())
            mockCipher.init(any(), any<SecretKey>(), any<AlgorithmParameterSpec>())
        }
    }

    @Test
    fun `A Cipher exception returns a failure`() = runTest {
        val exception = NoSuchAlgorithmException("Algo exception")
        coEvery { Cipher.getInstance(any()) } throws exception

        useCase(
            keystoreKeyConfig = mockPassConfig,
            initializationVector = initializationVector
        ).assertErrorType(KeystoreError.General::class)
    }

    @Test
    fun `A SecretKey exception returns a failure`() = runTest {
        val keyError = KeystoreError.createGeneralError()
        coEvery { mockGetOrCreateSecretKey(any()) } returns Err(keyError)

        useCase(
            keystoreKeyConfig = mockPassConfig,
            initializationVector = initializationVector
        ).assertErrorType(KeystoreError.General::class)
    }
}
