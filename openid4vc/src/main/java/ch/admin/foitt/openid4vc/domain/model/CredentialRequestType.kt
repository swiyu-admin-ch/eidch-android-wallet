package ch.admin.foitt.openid4vc.domain.model

sealed interface CredentialRequestType {
    val request: String

    data class Json(override val request: String) : CredentialRequestType
    data class Jwt(override val request: String) : CredentialRequestType
}
