package ch.admin.foitt.openid4vc.domain.model.presentationRequest

import ch.admin.foitt.openid4vc.domain.model.jwk.Jwks
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

internal class ClientMetaDataSerializer : KSerializer<ClientMetaData> {

    private val stringToJsonElementSerializer = MapSerializer(String.serializer(), JsonElement.serializer())
    override val descriptor: SerialDescriptor = stringToJsonElementSerializer.descriptor

    override fun deserialize(decoder: Decoder): ClientMetaData {
        require(decoder is JsonDecoder)

        val clientMetaDataMap = decoder.decodeSerializableValue(stringToJsonElementSerializer)
            .map { it.key to it.value }
            .toMap()

        val clientNameList = mutableListOf<ClientName>()
        val logoUriList = mutableListOf<LogoUri>()

        clientMetaDataMap.forEach { entry ->
            when {
                entry.key.contains(CLIENT_NAME) -> {
                    val (content, locale) = entry.key.split(DELIMITER).let {
                        Pair(entry.value.jsonPrimitive.content, it.getOrNull(1) ?: FALLBACK)
                    }
                    clientNameList.add(ClientName(clientName = content, locale = locale))
                }

                entry.key.contains(LOGO_URI) -> {
                    val (content, locale) = entry.key.split(DELIMITER).let {
                        Pair(entry.value.jsonPrimitive.content, it.getOrNull(1) ?: FALLBACK)
                    }
                    logoUriList.add(LogoUri(logoUri = content, locale = locale))
                }
            }
        }

        val jwksJson = clientMetaDataMap[JWKS]
        val jwks = if (jwksJson != null && jwksJson != JsonNull) {
            decoder.json.decodeFromJsonElement(Jwks.serializer(), jwksJson)
        } else {
            null
        }

        val encryptedResponseEncValuesSupportedJson = clientMetaDataMap[ENCRYPTED_RESPONSE_ENC_VALUES_SUPPORTED]
        val encryptedResponseEncValuesSupported =
            if (encryptedResponseEncValuesSupportedJson != null && encryptedResponseEncValuesSupportedJson != JsonNull) {
                encryptedResponseEncValuesSupportedJson.jsonArray.map { it.jsonPrimitive.content }
            } else {
                null
            }

        return ClientMetaData(
            clientNameList = clientNameList,
            logoUriList = logoUriList,
            jwks = jwks,
            encryptedResponseEncValuesSupported = encryptedResponseEncValuesSupported,
        )
    }

    override fun serialize(encoder: Encoder, value: ClientMetaData) {
        require(encoder is JsonEncoder)

        val element = buildJsonObject {
            value.clientNameList.forEach {
                put(if (it.locale != FALLBACK) "$CLIENT_NAME#${it.locale}" else CLIENT_NAME, JsonPrimitive(it.clientName))
            }
            value.logoUriList.forEach {
                put(if (it.locale != FALLBACK) "$LOGO_URI#${it.locale}" else LOGO_URI, JsonPrimitive(it.logoUri))
            }
            value.jwks?.let {
                put(JWKS, encoder.json.encodeToJsonElement(Jwks.serializer(), it))
            }
            value.encryptedResponseEncValuesSupported?.let {
                if (it.isNotEmpty()) {
                    put(
                        key = ENCRYPTED_RESPONSE_ENC_VALUES_SUPPORTED,
                        element = JsonArray(it.map { encValue -> JsonPrimitive(encValue) })
                    )
                }
            }
        }
        encoder.encodeJsonElement(element)
    }

    companion object {
        private const val DELIMITER = "#"
        private const val FALLBACK = "fallback"
        private const val LOGO_URI = "logo_uri"
        private const val CLIENT_NAME = "client_name"
        private const val JWKS = "jwks"
        private const val ENCRYPTED_RESPONSE_ENC_VALUES_SUPPORTED = "encrypted_response_enc_values_supported"
    }
}
