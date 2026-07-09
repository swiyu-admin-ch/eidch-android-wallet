package ch.admin.foitt.openid4vc.domain.model.presentationRequest

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface AuthorizationResponse {
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
        ACCESS_DENIED("access_denied"),
        INVALID_REQUEST("invalid_request"),
        INVALID_CLIENT("invalid_client")
    }
}
