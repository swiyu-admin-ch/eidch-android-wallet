package ch.admin.foitt.openid4vc.utils

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders

fun HttpRequestBuilder.acceptLanguageHeader() {
    val supportedLocales = listOf("de-CH", "en", "fr-CH", "it-CH", "rm")
    val acceptLanguageHeaderValue = supportedLocales.joinToString(separator = ", ") { it }
    header(HttpHeaders.AcceptLanguage, acceptLanguageHeaderValue)
}
