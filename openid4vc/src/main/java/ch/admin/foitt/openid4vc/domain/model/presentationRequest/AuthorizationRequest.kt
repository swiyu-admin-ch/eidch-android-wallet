package ch.admin.foitt.openid4vc.domain.model.presentationRequest

import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
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
    val responseUri: String,
    @SerialName("nonce")
    val nonce: String,
    @SerialName("presentation_definition")
    val presentationDefinition: PresentationDefinition?,
    @SerialName("dcql_query")
    val dcqlQuery: DcqlQuery?,
    @SerialName("client_metadata")
    val clientMetaData: ClientMetaData?,
    @SerialName("state")
    val state: String?,
)

@Serializable
data class PresentationDefinition(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String?,
    @SerialName("input_descriptors")
    val inputDescriptors: List<InputDescriptor>,
    @SerialName("purpose")
    val purpose: String?
)

@Serializable
data class InputDescriptor(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String?,
    @Serializable(with = InputDescriptorFormatListSerializer::class)
    @SerialName("format")
    val formats: List<InputDescriptorFormat>,
    @SerialName("constraints")
    val constraints: Constraints,
    @SerialName("purpose")
    val purpose: String?
)

@Serializable
data class Constraints(
    @SerialName("fields")
    val fields: List<Field>
)

@Serializable
data class Field(
    @SerialName("filter")
    val filter: Filter? = null,
    @SerialName("path")
    val path: List<String>
)

@Serializable
data class Filter(
    @SerialName("const")
    val const: String?,
    @SerialName("type")
    val type: String
) {
    companion object {
        const val TYPE_STRING = "string"
        const val TYPE_NUMBER = "number"
    }
}

@Serializable(with = InputDescriptorFormatSerializer::class)
sealed class InputDescriptorFormat(open val name: String) {
    data class VcSdJwt(
        override val name: String = VC_SD_JWT_KEY,
        val sdJwtAlgorithms: List<SigningAlgorithm>,
        val kbJwtAlgorithms: List<SigningAlgorithm>?,
    ) : InputDescriptorFormat(name) {
        companion object {
            val VC_SD_JWT_KEY = CredentialFormat.VC_SD_JWT.format
            const val SDJWT_ALGORITHM_KEY = "sd-jwt_alg_values"
            const val KBJWT_ALGORITHM_KB_KEY = "kb-jwt_alg_values"
        }
    }
}

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
