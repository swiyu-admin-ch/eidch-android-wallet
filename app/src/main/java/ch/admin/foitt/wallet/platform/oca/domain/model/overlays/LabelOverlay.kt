package ch.admin.foitt.wallet.platform.oca.domain.model.overlays

import ch.admin.foitt.wallet.platform.oca.domain.model.OverlaySpecType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface LabelOverlay : LocalizedOverlay

/**
 * Label Overlay
 * https://oca.colossi.network/specification/#label-overlay
 *
 * @property attributeLabels A map of attribute names to their localized labels.
 */
@Serializable
data class LabelOverlay1x0(
    @SerialName("capture_base")
    override val captureBaseDigest: String,
    @SerialName("language")
    override val language: String,

    @SerialName("attribute_labels")
    val attributeLabels: Map<String, String>,
    @SerialName("attribute_categories")
    val attributeCategories: List<String> = emptyList(),
    @SerialName("category_labels")
    val categoryLabels: Map<String, String> = emptyMap()
) : LabelOverlay {
    @SerialName("type")
    override val type: OverlaySpecType = OverlaySpecType.LABEL_1_0
}

/**
 * Same as version 1.0, but also supports templating in the labels
 */
@Serializable
data class LabelOverlay1x1(
    @SerialName("capture_base")
    override val captureBaseDigest: String,
    @SerialName("language")
    override val language: String,

    @SerialName("attribute_labels")
    val attributeLabels: Map<String, String>,
    @SerialName("attribute_categories")
    val attributeCategories: List<String> = emptyList(),
    @SerialName("category_labels")
    val categoryLabels: Map<String, String> = emptyMap()
) : LabelOverlay {
    @SerialName("type")
    override val type: OverlaySpecType = OverlaySpecType.LABEL_1_1
}
