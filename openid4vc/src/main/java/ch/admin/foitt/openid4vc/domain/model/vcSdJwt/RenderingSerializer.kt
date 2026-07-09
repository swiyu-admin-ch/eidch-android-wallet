package ch.admin.foitt.openid4vc.domain.model.vcSdJwt

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.jsonObject

internal object RenderingSerializer : KSerializer<VcSdJwtOcaRendering?> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Rendering")

    override fun deserialize(decoder: Decoder): VcSdJwtOcaRendering? {
        val jsonDecoder = decoder as JsonDecoder
        val element = jsonDecoder.decodeJsonElement()

        val ocaElement = element.jsonObject[VcSdJwtOcaRendering.RENDERING_METHOD_OCA_KEY] ?: return null

        return jsonDecoder.json.decodeFromJsonElement(
            deserializer = VcSdJwtOcaRendering.serializer(),
            element = ocaElement,
        )
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(
        encoder: Encoder,
        value: VcSdJwtOcaRendering?,
    ) {
        if (value == null) {
            encoder.encodeNull()
            return
        }

        encoder.encodeSerializableValue(
            serializer = VcSdJwtOcaRendering.serializer(),
            value = value,
        )
    }
}
