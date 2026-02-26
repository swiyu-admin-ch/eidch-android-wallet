package ch.admin.foitt.openid4vc.domain.model.jwk

import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.nimbusds.jose.jwk.ECKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * https://datatracker.ietf.org/doc/html/rfc7517
 */
@Serializable
data class Jwk(
    @SerialName("x")
    val x: String,
    @SerialName("y")
    val y: String,
    @SerialName("crv")
    val crv: String,
    @SerialName("kty")
    val kty: String,
    @SerialName("kid")
    val kid: String? = null,
    @SerialName("x5c")
    val x5c: List<String>? = null,
    @SerialName("use")
    val use: String? = null,
    @SerialName("alg")
    val alg: String? = null,
) {
    companion object {
        fun fromEcKey(
            ecKeyString: String,
            certificateChainBase64: List<String>?,
        ) = runSuspendCatching {
            ECKey.parse(ecKeyString).toEcJwk(certificateChainBase64)
        }
    }
}

fun ECKey.toEcJwk(certificateChainBase64: List<String>?): Jwk = Jwk(
    x = x.toString(),
    y = y.toString(),
    crv = curve.name,
    kid = keyID,
    kty = keyType.value,
    x5c = certificateChainBase64,
)

fun Jwk.hasSameCurveAs(otherJwk: Jwk): Boolean =
    kty == otherJwk.kty &&
        crv == otherJwk.crv &&
        x == otherJwk.x &&
        y == otherJwk.y
