package ch.admin.foitt.openid4vc.domain.model.vcSdJwt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * reference: https://www.ietf.org/archive/id/draft-ietf-oauth-sd-jwt-vc-05.html#name-display-metadata
 */
@Serializable
data class TypeMetadata(
    @SerialName("vct")
    val vct: String,
    @SerialName("vct#integrity")
    val vctIntegrity: String?,
    @SerialName("name")
    val name: String?,
    @SerialName("description")
    val description: String?,
    @SerialName("extends")
    val extends: String?,
    @SerialName("display")
    val displays: List<TypeMetadataDisplay>?,
    @SerialName("schema_uri")
    val schemaUri: String?,
    @SerialName("schema_uri#integrity")
    val schemaUriIntegrity: String?,
)

@Serializable
data class TypeMetadataDisplay(
    @SerialName("lang")
    val lang: String,
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String?,
    @Serializable(with = RenderingSerializer::class)
    @SerialName("rendering")
    val rendering: VcSdJwtOcaRendering?,
)

fun List<TypeMetadataDisplay>?.getFirstRendering() = this?.firstNotNullOfOrNull { it.rendering }

@Serializable
data class VcSdJwtOcaRendering(
    @SerialName("uri")
    val uri: String,
    @SerialName("uri#integrity")
    val uriIntegrity: String?,
) {
    companion object {
        const val RENDERING_METHOD_OCA_KEY = "oca"
    }
}
