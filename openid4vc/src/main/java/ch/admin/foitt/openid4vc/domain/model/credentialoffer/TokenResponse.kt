package ch.admin.foitt.openid4vc.domain.model.credentialoffer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TokenResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("expires_in")
    val expiresIn: Int? = null,
    @SerialName("refresh_token")
    val refreshToken: String? = null,
    @SerialName("scope")
    val scope: String? = null,
    @SerialName("token_type")
    val tokenType: String
)
