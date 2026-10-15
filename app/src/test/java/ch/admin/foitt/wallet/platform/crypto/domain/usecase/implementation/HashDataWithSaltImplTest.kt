package ch.admin.foitt.wallet.platform.crypto.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.crypto.domain.model.HashConfiguration
import ch.admin.foitt.wallet.platform.crypto.domain.usecase.HashDataWithSalt
import ch.admin.foitt.wallet.platform.crypto.domain.usecase.implementation.mock.MockHashConfiguration.HASH_ALGORITHM
import ch.admin.foitt.wallet.platform.crypto.domain.usecase.implementation.mock.MockHashConfiguration.HASH_ITERATIONS
import ch.admin.foitt.wallet.platform.crypto.domain.usecase.implementation.mock.MockHashConfiguration.HASH_KEY_LENGTH
import ch.admin.foitt.wallet.platform.crypto.domain.usecase.implementation.mock.MockHashConfiguration.SALT_LENGTH
import ch.admin.foitt.wallet.util.assertErr
import ch.admin.foitt.wallet.util.assertOk
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class HashDataWithSaltImplTest {
    @MockK
    private lateinit var hashConfiguration: HashConfiguration

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var useCase: HashDataWithSalt

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = HashDataWithSaltImpl(
            hashConfiguration = hashConfiguration,
            ioDispatcher = testDispatcher,
        )

        every { hashConfiguration.hashAlgorithm } returns HASH_ALGORITHM
        every { hashConfiguration.hashIterations } returns HASH_ITERATIONS
        every { hashConfiguration.hashKeyLength } returns HASH_KEY_LENGTH
        every { hashConfiguration.saltLength } returns SALT_LENGTH
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `hashing with salt should use default hash configuration`() = runTest(testDispatcher) {
        val hashData = useCase(DATA, salt).assertOk()

        verify(exactly = 0) { hashConfiguration.saltLength }

        assertEquals(salt.size, hashData.salt.size)
        assertEquals(hashConfiguration.hashKeyLength / 8, hashData.hash.size) // divided keyLength in bits by 8 to get bytes
        // divided keyLength in bits by 8 to get bytes; hex-string uses 2 bytes per position
        assertEquals(hashConfiguration.hashKeyLength / 8, hashData.hashHexString.length / 2)
    }

    @Test
    fun `hashing with salt should use given hash configuration`() = runTest(testDispatcher) {
        val keyLength = 128
        every { hashConfiguration.hashKeyLength } returns keyLength

        val hashData = useCase(DATA, salt).assertOk()

        verify(exactly = 0) { hashConfiguration.saltLength }
        assertEquals(salt.size, hashData.salt.size)
        assertEquals(keyLength / 8, hashData.hash.size) // divided keyLength in bits by 8 to get bytes
        // divided keyLength in bits by 8 to get bytes; hex-string uses 2 bytes per position
        assertEquals(keyLength / 8, hashData.hashHexString.length / 2)
    }

    @Test
    fun `hashing with salt should fail for unknown hash algorithm`() = runTest(testDispatcher) {
        every { hashConfiguration.hashAlgorithm } returns "unknown"

        useCase(DATA, salt).assertErr()
    }

    @Test
    fun `hashing with salt should fail for zero salt size`() = runTest(testDispatcher) {
        val salt = "".toByteArray()

        useCase(DATA, salt).assertErr()
    }

    @Test
    fun `hashing with salt should fail for zero hash key length`() = runTest(testDispatcher) {
        every { hashConfiguration.hashKeyLength } returns 0

        useCase(DATA, salt).assertErr()
    }

    @Test
    fun `hashing with salt two times with same data but different salt should not generate same hash`() = runTest(testDispatcher) {
        val hashData = useCase(DATA, salt).assertOk()
        val hashData2 = useCase(DATA, "anotherSalt".toByteArray()).assertOk()

        assertNotEquals(hashData, hashData2)
        assertFalse(hashData.hash.contentEquals(hashData2.hash))
        assertNotEquals(hashData.hashHexString, hashData2.hashHexString)
        assertFalse(hashData.salt.contentEquals(hashData2.salt))
    }

    @Test
    fun `hashing with salt two times with different data but same salt should not generate same hash`() = runTest(testDispatcher) {
        val hashData = useCase(DATA, salt).assertOk()
        val hashData2 = useCase("anotherData", salt).assertOk()

        assertNotEquals(hashData, hashData2)
        assertFalse(hashData.hash.contentEquals(hashData2.hash))
        assertNotEquals(hashData.hashHexString, hashData2.hashHexString)
        assertTrue(hashData.salt.contentEquals(hashData2.salt))
    }

    @Test
    fun `hashing with salt two times with same data and salt should generate same hash`() = runTest(testDispatcher) {
        val hashData = useCase(DATA, salt).assertOk()
        val hashData2 = useCase(DATA, salt).assertOk()

        assertEquals(hashData, hashData2)
        assertTrue(hashData.hash.contentEquals(hashData2.hash))
        assertEquals(hashData.hashHexString, hashData2.hashHexString)
        assertTrue(hashData.salt.contentEquals(hashData2.salt))
    }

    private companion object {
        const val DATA = "data"
        val salt = "salt".toByteArray()
    }
}
