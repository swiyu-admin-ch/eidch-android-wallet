package ch.admin.foitt.openid4vc.domain.model.presentationRequest

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface AuthorizationResponse {
    @Serializable
    data class Dif(
        @SerialName("vp_token")
        val vpToken: String,
        @SerialName("presentation_submission")
        val presentationSubmission: PresentationSubmission,
        @SerialName("state")
        val state: String? = null,
    ) : AuthorizationResponse

    @Serializable
    data class Dcql(
        @SerialName("vp_token")
        val vpToken: Map<String, List<String>>,
        @SerialName("state")
        val state: String? = null,
    ) : AuthorizationResponse
}

@Serializable
data class AuthorizationResponseErrorBody(
    @SerialName("error")
    val error: ErrorType,
    @SerialName("error_description")
    val errorDescription: String? = null,
    @SerialName("state")
    val state: String? = null,
) {
    enum class ErrorType(val key: String) {
        CLIENT_REJECTED("client_rejected"),
        INVALID_REQUEST("invalid_request")
    }
}

@Serializable
data class PresentationSubmission(
    @SerialName("definition_id")
    val definitionId: String,
    @SerialName("descriptor_map")
    val descriptorMap: List<DescriptorMap>,
    @SerialName("id")
    val id: String
)

@Serializable
data class DescriptorMap(
    @SerialName("format")
    val format: String,
    @SerialName("id")
    val id: String,
    @SerialName("path")
    val path: String,
)
