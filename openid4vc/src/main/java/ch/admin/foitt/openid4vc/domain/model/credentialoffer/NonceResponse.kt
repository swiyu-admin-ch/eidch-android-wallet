package ch.admin.foitt.openid4vc.domain.model.credentialoffer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NonceResponse(
    @SerialName("c_nonce")
    val cNonce: String,
)
