package ch.admin.foitt.wallet.platform.oca.domain.model.overlays

import ch.admin.foitt.wallet.platform.oca.domain.model.OverlaySpecType
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Represents a generic Overlay that associates metadata with a Capture Base.
 *
 * @property type The Overlay specification type.
 * @property captureBaseDigest The digest of the associated Capture Base.
 */
@Serializable(with = OverlaySerializer::class)
sealed interface Overlay {
    @SerialName("type")
    val type: OverlaySpecType

    @SerialName("capture_base")
    val captureBaseDigest: String
}

private object OverlaySerializer : JsonContentPolymorphicSerializer<Overlay>(
    Overlay::class
) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<Overlay> {
        return when (OverlaySpecType.getByType(element.jsonObject["type"]?.jsonPrimitive?.content)) {
            OverlaySpecType.DATA_SOURCE_1_0 -> DataSourceOverlay1x0.serializer()
            OverlaySpecType.DATA_SOURCE_2_0 -> DataSourceOverlay2x0.serializer()
            OverlaySpecType.LABEL_1_0 -> LabelOverlay1x0.serializer()
            OverlaySpecType.BRANDING_1_1 -> BrandingOverlay1x1.serializer()
            OverlaySpecType.META_1_0 -> MetaOverlay1x0.serializer()
            OverlaySpecType.CHARACTER_ENCODING_1_0 -> CharacterEncodingOverlay1x0.serializer()
            OverlaySpecType.FORMAT_1_0 -> FormatOverlay1x0.serializer()
            OverlaySpecType.STANDARD_1_0 -> StandardOverlay1x0.serializer()
            OverlaySpecType.ORDER_1_0 -> OrderOverlay1x0.serializer()
            OverlaySpecType.ENTRY_1_0 -> EntryOverlay1x0.serializer()
            OverlaySpecType.ENTRY_CODE_1_0 -> EntryCodeOverlay1x0.serializer()
            OverlaySpecType.SENSITIVE_1_0 -> SensitiveOverlay1x0.serializer()
            OverlaySpecType.UNSUPPORTED -> UnsupportedOverlayItem.serializer()
        }
    }
}
