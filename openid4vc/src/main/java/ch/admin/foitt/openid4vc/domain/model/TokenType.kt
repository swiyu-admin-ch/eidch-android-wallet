package ch.admin.foitt.openid4vc.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TokenType {
    BEARER,

    @SerialName("DPoP")
    DPOP,
}
