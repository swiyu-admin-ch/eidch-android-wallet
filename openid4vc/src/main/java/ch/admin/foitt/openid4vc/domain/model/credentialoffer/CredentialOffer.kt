package ch.admin.foitt.openid4vc.domain.model.credentialoffer

import ch.admin.foitt.openid4vc.domain.model.HttpsURLAsStringSerializer
import ch.admin.foitt.openid4vc.domain.model.Invitation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.net.URL

// Spec: https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0-13.html#section-4.1.1-2.2

@Serializable
data class CredentialOffer(
    @Serializable(HttpsURLAsStringSerializer::class)
    @SerialName("credential_issuer")
    val credentialIssuer: URL,
    @SerialName("credential_configuration_ids")
    val credentialConfigurationIds: List<String>,
    val grants: Grant,
) : Invitation

@Serializable
data class Grant(
    @SerialName("urn:ietf:params:oauth:grant-type:refresh_token")
    val refreshToken: String? = null,
    @SerialName("urn:ietf:params:oauth:grant-type:pre-authorized_code")
    val preAuthorizedCode: PreAuthorizedContent? = null,
    @SerialName("urn:ietf:params:oauth:grant-type:authorized_code")
    val authorizedCode: AuthorizedContent? = null
)

@Serializable
data class PreAuthorizedContent(
    @SerialName("pre-authorized_code")
    val preAuthorizedCode: String,
    @SerialName("user_pin_required")
    val isUserPinRequired: Boolean = false
)

@Serializable
data class AuthorizedContent(
    @SerialName("issuer_state")
    val issuerState: String? = null
)
