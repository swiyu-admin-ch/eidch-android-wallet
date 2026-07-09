package ch.admin.foitt.openid4vc.domain.model.presentationRequest

import ch.admin.foitt.openid4vc.domain.model.jwk.Jwks
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uniffi.heidi_dcql_rust.DcqlQuery

@Serializable
data class AuthorizationRequest(
    @SerialName("client_id")
    val clientId: String,
    @SerialName("response_type")
    val responseType: String,
    @SerialName("response_mode")
    val responseMode: String,
    @SerialName("response_uri")
    val responseUri: String?,
    @SerialName("nonce")
    val nonce: String,
    @SerialName("dcql_query")
    val dcqlQuery: DcqlQuery?,
    @SerialName("client_metadata")
    val clientMetaData: ClientMetaData?,
    @SerialName("state")
    val state: String?,
    @SerialName("expected_origins")
    val expectedOrigins: List<String>?
)

@Serializable(with = ClientMetaDataSerializer::class)
data class ClientMetaData(
    val clientNameList: List<ClientName>,
    val logoUriList: List<LogoUri>,
    val jwks: Jwks? = null,
    val encryptedResponseEncValuesSupported: List<String>? = null,
)

data class ClientName(
    val clientName: String,
    val locale: String,
)

data class LogoUri(
    val logoUri: String,
    val locale: String,
)
