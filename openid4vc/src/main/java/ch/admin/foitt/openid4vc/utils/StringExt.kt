package ch.admin.foitt.openid4vc.utils

import ch.admin.foitt.openid4vc.domain.model.DigestAlgorithm
import java.security.MessageDigest
import java.util.Base64

internal fun String.createDigest(algorithm: DigestAlgorithm): String {
    val digest = MessageDigest.getInstance(algorithm.stdName)
    val bytes = digest.digest(toByteArray(Charsets.US_ASCII))
    return bytes.toBase64StringUrlEncodedWithoutPadding()
}

fun String.base64ToDecodedString() = String(Base64.getDecoder().decode(this))
