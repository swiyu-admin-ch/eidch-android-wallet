package ch.admin.foitt.openid4vc.domain.model.presentationRequest

data class AuthorizationResponseConfig(
    val type: AuthorizationResponseType,
    val params: Map<AuthorizationResponseParam, String>,
)

enum class AuthorizationResponseType {
    DCQL
}

enum class AuthorizationResponseParam(val jsonName: String) {
    VP_TOKEN("vp_token"),
    RESPONSE("response"),
    STATE("state"),
}
