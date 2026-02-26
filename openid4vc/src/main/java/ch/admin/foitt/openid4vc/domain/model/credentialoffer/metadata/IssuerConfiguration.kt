package ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata

import ch.admin.foitt.openid4vc.domain.model.HttpsURLAsStringSerializer
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.net.URL

sealed interface IssuerConfigurationResponse {
    data class Signed(val jwt: Jwt, val config: IssuerConfiguration) : IssuerConfigurationResponse
    data class Plain(val config: IssuerConfiguration) : IssuerConfigurationResponse
}

@Serializable
data class IssuerConfiguration(
    @Serializable(HttpsURLAsStringSerializer::class)
    @SerialName("issuer")
    val issuer: URL,
    @Serializable(HttpsURLAsStringSerializer::class)
    @SerialName("token_endpoint")
    val tokenEndpoint: URL,
)
