package ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object AnyCredentialConfigurationSerializer : JsonContentPolymorphicSerializer<AnyCredentialConfiguration>(
    AnyCredentialConfiguration::class
) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<AnyCredentialConfiguration> {
        return when (element.jsonObject["format"]?.jsonPrimitive?.content) {
            CredentialFormat.VC_SD_JWT.format, CredentialFormat.DC_SD_JWT.format -> {
                VcSdJwtCredentialConfiguration.serializer()
            }
            else -> UnknownCredentialConfiguration.serializer()
        }
    }
}
