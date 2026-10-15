package ch.admin.foitt.wallet.platform.versionEnforcement.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JwsDetachedHeader(
    @SerialName("alg") val alg: String,
    @SerialName("kid") val kid: String? = null,
    @SerialName("b64") val b64: Boolean? = null,
    @SerialName("crit") val crit: List<String>? = null,
)
