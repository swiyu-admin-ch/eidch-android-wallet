package ch.admin.foitt.wallet.platform.passphrase

import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.model.KeystoreKeyConfig
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.usecase.GetOrCreateSecretKey
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.usecase.implementation.GetCipherForDecryptionImpl
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.usecase.implementation.GetCipherForEncryptionImpl
import ch.admin.foitt.wallet.platform.passphrase.domain.model.CiphertextWrapper
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.repository.PassphraseRepository
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.EncryptAndSavePassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.LoadAndDecryptPassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.implementation.EncryptAndSavePassphraseImpl
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.implementation.LoadAndDecryptPassphraseImpl
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

class EncryptDecryptPassphraseTest {
    @MockK
    private lateinit var mockGetOrCreateSecretKey: GetOrCreateSecretKey

    @MockK
    private lateinit var mockCipher: Cipher

    @MockK
    private lateinit var mockPassConfig: KeystoreKeyConfig

    private lateinit var decryptionUseCase: LoadAndDecryptPassphrase
    private lateinit var encryptionUseCase: EncryptAndSavePassphrase

    private lateinit var currentDecryptionCipher: Cipher
    private lateinit var currentEncryptionCipher: Cipher

    private val encryptionTransformation = "AES_256/GCM/NoPadding"
    private val algorithmName = "AES"
    private val keyBytes = Random(0).nextBytes(32)
    private val gcmTagLength = 128

    private fun createSecretKey(): SecretKey = SecretKeySpec(keyBytes, algorithmName)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        val secretKey = createSecretKey()

        coEvery { mockGetOrCreateSecretKey.invoke(keystoreKeyConfig = mockPassConfig) } returns Ok(
            secretKey
        )
        every { mockPassConfig.encryptionTransformation } returns encryptionTransformation
        every { mockPassConfig.gcmAuthTagLength } returns gcmTagLength
        every { mockPassConfig.randomizedEncryptionRequired } returns true

        encryptionUseCase = EncryptAndSavePassphraseImpl(
            passphraseRepository = testPassphraseRepository
        )

        decryptionUseCase = LoadAndDecryptPassphraseImpl(
            passphraseRepository = testPassphraseRepository,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "",
            "KodGmxyJBHR&mZx3jpa_¶j3SZT@n^oaxUyVgT4QhEYPPqGcazdh9ndNgXW2!AbW2igRW6XEakUnzpZW",
            "0",
            "-1",
            " ",
            "!@#%^&*()_+-=[]{}|;':\",./<>?~`$¶",
        ]
    )
    fun `A passphrase is encrypted and decrypted successfully`(passphrase: String) = runTest {
        initializeCiphers()

        encryptionUseCase(
            passphrase = passphrase.toByteArray(Charsets.UTF_8),
            encryptionCipher = currentEncryptionCipher
        ).assertOk()

        val decryptionResult = decryptionUseCase(
            decryptionCipher = currentDecryptionCipher
        ).assertOk()

        assertEquals(passphrase, String(decryptionResult))
    }

    @Test
    fun `A encryption exception returns a failure`() = runTest {
        val exception = Exception("CipherError")
        coEvery { mockCipher.doFinal(any()) } throws exception

        encryptionUseCase(
            passphrase = "passphrase".toByteArray(Charsets.UTF_8),
            encryptionCipher = mockCipher,
        ).assertErrorType(PassphraseError.General::class)
    }

    @Test
    fun `An decryption exception returns a failure`() = runTest {
        val exception = Exception("CipherError")
        coEvery { mockCipher.doFinal(any()) } throws exception

        decryptionUseCase(
            decryptionCipher = mockCipher
        ).assertErrorType(PassphraseError.General::class)
    }

    private fun initializeCiphers() {
        val getCipherForEncryptionImpl = GetCipherForEncryptionImpl(
            mockGetOrCreateSecretKey,
        )

        val getCipherForDecryptionImpl = GetCipherForDecryptionImpl(
            mockGetOrCreateSecretKey,
        )

        currentEncryptionCipher = runBlocking {
            getCipherForEncryptionImpl(
                keystoreKeyConfig = mockPassConfig, initializationVector = null
            ).assertOk()
        }
        testPassphraseRepository.currentIv = currentEncryptionCipher.iv
        currentDecryptionCipher = runBlocking {
            getCipherForDecryptionImpl(
                keystoreKeyConfig = mockPassConfig, initializationVector = testPassphraseRepository.currentIv
            ).assertOk()
        }
    }

    private val testPassphraseRepository = object : PassphraseRepository {
        var currentPassphraseCipherText: ByteArray = byteArrayOf()
        var currentIv: ByteArray = byteArrayOf()

        override suspend fun getPassphrase() = CiphertextWrapper(
            ciphertext = currentPassphraseCipherText,
            initializationVector = currentIv,
        )

        override suspend fun savePassphrase(passphraseWrapper: CiphertextWrapper) {
            currentPassphraseCipherText = passphraseWrapper.ciphertext
            currentIv = passphraseWrapper.initializationVector
        }

        override suspend fun deletePassphrase() {
            TODO("Not yet implemented")
        }

        override suspend fun savePassphraseWasDeleted(passphraseWasDeleted: Boolean) {
            TODO("Not yet implemented")
        }

        override suspend fun getPassphraseWasDeleted(): Boolean {
            TODO("Not yet implemented")
        }
    }
}
