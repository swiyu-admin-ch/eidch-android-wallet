package ch.admin.foitt.openid4vc.utils

import ch.admin.foitt.openid4vc.domain.model.CredentialRequestType
import ch.admin.foitt.openid4vc.utils.ContentType.applicationJwt
import io.ktor.http.ContentType

object ContentType {
    val applicationJwt = ContentType(ContentType.Application.TYPE, "jwt")
}

val ContentType.content: String
    get() = "$contentType/$contentSubtype"

fun CredentialRequestType.toContentType(): ContentType = when (this) {
    is CredentialRequestType.Json -> ContentType.Application.Json
    is CredentialRequestType.Jwt -> applicationJwt
}
