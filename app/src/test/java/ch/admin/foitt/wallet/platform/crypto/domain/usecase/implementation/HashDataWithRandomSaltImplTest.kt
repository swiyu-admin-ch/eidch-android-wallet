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
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class HashDataWithRandomSaltImplTest {

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
    fun `hashing with random salt should use default hash configuration`() = runTest(testDispatcher) {
        val hashData = useCase(data = DATA, salt = null).assertOk()

        assertEquals(SALT_LENGTH, hashData.salt.size)
        assertEquals(HASH_KEY_LENGTH / 8, hashData.hash.size) // divided keyLength in bits by 8 to get bytes
        // divided keyLength in bits by 8 to get bytes; hex-string uses 2 bytes per position
        assertEquals(HASH_KEY_LENGTH / 8, hashData.hashHexString.length / 2)
    }

    @Test
    fun `hashing with random salt should use given hash configuration`() = runTest(testDispatcher) {
        val saltLength = 4
        val keyLength = 128
        every { hashConfiguration.saltLength } returns saltLength
        every { hashConfiguration.hashKeyLength } returns keyLength

        val hashData = useCase(data = DATA, salt = null).assertOk()

        assertEquals(saltLength, hashData.salt.size)
        assertEquals(keyLength / 8, hashData.hash.size) // divided keyLength in bits by 8 to get bytes
        // divided keyLength in bits by 8 to get bytes; hex-string uses 2 bytes per position
        assertEquals(keyLength / 8, hashData.hashHexString.length / 2)
    }

    @Test
    fun `hashing with random salt should fail for unknown hash algorithm`() = runTest(testDispatcher) {
        every { hashConfiguration.hashAlgorithm } returns "unknown"

        useCase.invoke(DATA, salt = null).assertErr()
    }

    @Test
    fun `hashing with random salt should fail for zero salt size`() = runTest(testDispatcher) {
        every { hashConfiguration.saltLength } returns 0

        useCase(DATA, salt = null).assertErr()
    }

    @Test
    fun `hashing with random salt should fail for zero hash key length`() = runTest(testDispatcher) {
        every { hashConfiguration.hashKeyLength } returns 0

        useCase(DATA, salt = null).assertErr()
    }

    @Test
    fun `hashing with random salt two times with same data should not generate same hash`() = runTest(testDispatcher) {
        val hashData = useCase(DATA, null).assertOk()
        val hashData2 = useCase(DATA, null).assertOk()

        assertNotEquals(hashData, hashData2)
        assertFalse(hashData.hash.contentEquals(hashData2.hash))
        assertNotEquals(hashData.hashHexString, hashData2.hashHexString)
        assertFalse(hashData.salt.contentEquals(hashData2.salt))
    }

    @Test
    fun `hashing with random salt two times with different data should not generate same hash`() = runTest(testDispatcher) {
        val hashData = useCase(DATA, null).assertOk()
        val hashData2 = useCase("otherData", null).assertOk()

        assertNotEquals(hashData, hashData2)
        assertFalse(hashData.hash.contentEquals(hashData2.hash))
        assertNotEquals(hashData.hashHexString, hashData2.hashHexString)
        assertFalse(hashData.salt.contentEquals(hashData2.salt))
    }

    private companion object {
        const val DATA = "data"
    }
}
