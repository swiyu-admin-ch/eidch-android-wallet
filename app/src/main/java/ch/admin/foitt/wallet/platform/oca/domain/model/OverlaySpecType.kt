package ch.admin.foitt.wallet.platform.oca.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Enum representing different types of Overlay specifications in OCA.
 *
 * @property type The string representation of the Overlay specification type.
 */
@Serializable
enum class OverlaySpecType(val type: String) {
    @SerialName("extend/overlays/data_source/1.0")
    DATA_SOURCE_1_0("extend/overlays/data_source/1.0"),

    @SerialName("extend/overlays/data_source/2.0")
    DATA_SOURCE_2_0("extend/overlays/data_source/2.0"),

    @SerialName("spec/overlays/label/1.0")
    LABEL_1_0("spec/overlays/label/1.0"),

    @SerialName("spec/overlays/character_encoding/1.0")
    CHARACTER_ENCODING_1_0("spec/overlays/character_encoding/1.0"),

    @SerialName("spec/overlays/format/1.0")
    FORMAT_1_0("spec/overlays/format/1.0"),

    @SerialName("spec/overlays/standard/1.0")
    STANDARD_1_0("spec/overlays/standard/1.0"),

    @SerialName("spec/overlays/meta/1.0")
    META_1_0("spec/overlays/meta/1.0"),

    @SerialName("spec/overlays/entry_code/1.0")
    ENTRY_CODE_1_0("spec/overlays/entry_code/1.0"),

    @SerialName("spec/overlays/entry/1.0")
    ENTRY_1_0("spec/overlays/entry/1.0"),

    @SerialName("aries/overlays/branding/1.1")
    BRANDING_1_1("aries/overlays/branding/1.1"),

    @SerialName("extend/overlays/order/1.0")
    ORDER_1_0("extend/overlays/order/1.0"),

    @SerialName("spec/overlays/sensitive/1.0")
    SENSITIVE_1_0("spec/overlays/sensitive/1.0"),

    UNSUPPORTED("");

    companion object {
        fun getByType(type: String?): OverlaySpecType {
            return OverlaySpecType.entries.firstOrNull { it.type == type } ?: UNSUPPORTED
        }
    }
}
