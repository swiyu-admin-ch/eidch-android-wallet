package ch.admin.foitt.wallet.platform.appAttestation.domain.model

import ch.admin.foitt.openid4vc.domain.model.jwk.Jwk
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Confirmation(
    @SerialName("jwk")
    val jwk: Jwk,
)
