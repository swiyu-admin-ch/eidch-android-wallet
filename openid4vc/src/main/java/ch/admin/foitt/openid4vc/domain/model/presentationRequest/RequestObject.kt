package ch.admin.foitt.openid4vc.domain.model.presentationRequest

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt

data class RequestObject(
    val jwt: Jwt,
    val clientId: String?,
    val redirectUri: String?,
)
