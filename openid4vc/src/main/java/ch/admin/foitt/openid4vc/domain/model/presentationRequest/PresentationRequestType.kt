package ch.admin.foitt.openid4vc.domain.model.presentationRequest

sealed interface PresentationRequestType {
    data class Json(
        val vpToken: String,
        val presentationSubmission: String,
    ) : PresentationRequestType

    data class Jwt(
        val response: String // jwe
    ) : PresentationRequestType
}
