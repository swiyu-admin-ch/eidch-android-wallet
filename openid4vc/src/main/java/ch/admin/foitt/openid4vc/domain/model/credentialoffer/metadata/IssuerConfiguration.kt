package ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata

import ch.admin.foitt.openid4vc.domain.model.HttpsURLAsStringSerializer
import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
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
    @SerialName("dpop_signing_alg_values_supported")
    val dpopSigningAlgValuesSupported: List<SigningAlgorithm>? = null,
)

fun IssuerConfiguration.supportsDpop(supportedAlgorithms: Collection<SigningAlgorithm>): Boolean =
    dpopSigningAlgValuesSupported?.any { it in supportedAlgorithms } == true
