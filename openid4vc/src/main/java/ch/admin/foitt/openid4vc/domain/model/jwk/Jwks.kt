package ch.admin.foitt.openid4vc.domain.model.jwk

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Jwks(
    @SerialName("keys")
    val keys: List<Jwk>,
)
