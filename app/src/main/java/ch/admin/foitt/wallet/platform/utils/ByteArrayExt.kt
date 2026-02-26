package ch.admin.foitt.wallet.platform.utils

import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.zip.DeflaterOutputStream
import java.util.zip.Inflater
import java.util.zip.InflaterOutputStream

fun ByteArray.toBase64StringUrlEncodedWithoutPadding(): String =
    Base64.getUrlEncoder().withoutPadding().encodeToString(this)

fun ByteArray.toNonUrlEncodedBase64String(): String =
    Base64.getEncoder().encodeToString(this)

fun String.base64StringToByteArray(): ByteArray =
    Base64.getUrlDecoder().decode(this)

fun String.base64NonUrlStringToByteArray(): ByteArray =
    Base64.getDecoder().decode(this)

fun ByteArray.compress(): ByteArray {
    val output = ByteArrayOutputStream()
    DeflaterOutputStream(output).apply {
        write(this@compress)
        close()
    }
    return output.toByteArray()
}

fun ByteArray.decompress(): ByteArray {
    val output = ByteArrayOutputStream()
    InflaterOutputStream(output).apply {
        write(this@decompress)
        close()
    }
    return output.toByteArray()
}

/**
 * Deflate compressed data
 * @input: compressed data as byteArray
 * @output: decompressed data as byteArray
 * @Throws IllegalArgumentException if decompressed content is larger than maxSize
 */
fun ByteArray.decompressWithMaxSize(maximumSize: Int = 100 * 1024 * 1024): ByteArray {
    val bufferSize = 4 * 1024

    val output = ByteArrayOutputStream()
    val inflater = Inflater(false)

    InflaterOutputStream(output, inflater).use { inflaterOut ->
        var offset = 0
        val buffer = ByteArray(bufferSize)

        while (offset < this.size) {
            val readLength = minOf(bufferSize, this.size - offset)
            this.copyInto(
                destination = buffer,
                destinationOffset = 0,
                startIndex = offset,
                endIndex = offset + readLength,
            )

            inflaterOut.write(buffer, 0, readLength)
            offset += readLength

            if (output.size() > maximumSize) {
                val exception = IllegalStateException("Gzip decompression: content too large")
                Timber.e(t = exception, message = "Gzip decompression")
                throw exception
            }
        }
    }

    return output.toByteArray()
}
