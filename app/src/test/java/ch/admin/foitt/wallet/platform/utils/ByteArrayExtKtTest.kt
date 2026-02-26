package ch.admin.foitt.wallet.platform.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ByteArrayExtKtTest {
    @Test
    fun `compressing a string and uncompressing it should result in the same string`() {
        val input = "some random string to compress with ä, è, \u1234 or even ${Character.toString(0x1F44D)}"
        val compressed = input.toByteArray().compress()
        val uncompressed = compressed.decompress().decodeToString()
        assertEquals(input, uncompressed)
    }

    @Test
    fun `compressed data can be uncompressed`() {
        val size = 1 * 1024 * 1024
        val input = createByteArrayWithSize(size)
        val compressed = input.compress()

        assertTrue(input.contentEquals(compressed.decompressWithMaxSize(size)))
    }

    @Test
    fun `compressed data that is smaller than the chunk size (of 4096) can be uncompressed`() {
        val size = 4000
        val input = createByteArrayWithSize(size)
        val compressed = input.compress()

        assertTrue(input.contentEquals(compressed.decompressWithMaxSize(size)))
    }

    @Test
    fun `compression bomb is handled (uncompressed exceeds size limit)`() {
        val size = 1 * 1024 * 1024
        val input = createByteArrayWithSize(size)
        val compressed = input.compress()

        assertThrows<IllegalStateException> {
            compressed.decompressWithMaxSize(size - 1)
        }
    }

    private fun createByteArrayWithSize(size: Int): ByteArray {
        val array = ByteArray(size)
        for (i in 0 until size) {
            array[i] = 0
        }
        return array
    }
}
