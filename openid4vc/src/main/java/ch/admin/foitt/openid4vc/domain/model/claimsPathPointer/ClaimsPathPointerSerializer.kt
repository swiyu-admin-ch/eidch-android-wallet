package ch.admin.foitt.openid4vc.domain.model.claimsPathPointer

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

@OptIn(InternalSerializationApi::class)
object ClaimsPathPointerComponentSerializer : JsonContentPolymorphicSerializer<ClaimsPathPointerComponent>(
    ClaimsPathPointerComponent::class
) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<ClaimsPathPointerComponent> {
        val primitive = element.jsonPrimitive
        return when {
            primitive.isString -> ClaimsPathPointerComponent.String.serializer()
            primitive.intOrNull != null -> ClaimsPathPointerComponent.Index.serializer()
            else -> ClaimsPathPointerComponent.Null.serializer()
        }
    }
}

object ClaimsPathPointerComponentStringSerializer : KSerializer<ClaimsPathPointerComponent.String> {
    override val descriptor =
        PrimitiveSerialDescriptor(ClaimsPathPointerComponent.String::class.qualifiedName ?: "", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ClaimsPathPointerComponent.String) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): ClaimsPathPointerComponent.String {
        val name = decoder.decodeString()
        return ClaimsPathPointerComponent.String(name)
    }
}

object ClaimsPathPointerComponentIndexSerializer : KSerializer<ClaimsPathPointerComponent.Index> {
    override val descriptor =
        PrimitiveSerialDescriptor(ClaimsPathPointerComponent.Index::class.qualifiedName ?: "", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: ClaimsPathPointerComponent.Index) {
        encoder.encodeInt(value.index)
    }

    override fun deserialize(decoder: Decoder): ClaimsPathPointerComponent.Index {
        val index = decoder.decodeInt()
        check(index >= 0)
        return ClaimsPathPointerComponent.Index(index)
    }
}

@OptIn(ExperimentalSerializationApi::class)
object ClaimsPathPointerComponentNullSerializer : KSerializer<ClaimsPathPointerComponent.Null> {
    override val descriptor =
        PrimitiveSerialDescriptor(ClaimsPathPointerComponent.Null::class.qualifiedName ?: "", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ClaimsPathPointerComponent.Null) {
        encoder.encodeNull()
    }

    override fun deserialize(decoder: Decoder): ClaimsPathPointerComponent.Null {
        decoder.decodeNull()
        return ClaimsPathPointerComponent.Null
    }
}
