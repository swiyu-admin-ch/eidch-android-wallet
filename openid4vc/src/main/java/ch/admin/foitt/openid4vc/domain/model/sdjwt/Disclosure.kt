package ch.admin.foitt.openid4vc.domain.model.sdjwt

import kotlinx.serialization.json.JsonElement

sealed interface Disclosure {
    val value: JsonElement
    val disclosure: String

    data class KeyedElement(
        val key: String,
        override val value: JsonElement,
        override val disclosure: String,
    ) : Disclosure

    data class ArrayElement(
        override val value: JsonElement,
        override val disclosure: String,
    ) : Disclosure
}
