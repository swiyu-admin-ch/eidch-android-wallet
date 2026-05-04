package ch.admin.foitt.wallet.platform.oca.domain.model

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointer
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.Overlay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

typealias AttributeKey = String
typealias Locale = String
typealias DataSourceFormat = String
typealias EntryCode = String

@Serializable
data class OcaBundle(
    @SerialName("capture_bases") val captureBases: List<CaptureBase>,
    @SerialName("overlays") val overlays: List<Overlay>,
    @Transient val ocaClaimData: List<OcaClaimData> = emptyList(),
    @Transient val ocaCredentialData: List<OcaCredentialData> = emptyList(),
) {

    /**
     * Retrieves a specific Overlay Bundle attribute from the Capture Base.
     *
     * @param name The name of the Capture Base attribute.
     * @param digest An CESR digest of the associated Capture Base.
     * @return A [OcaClaimData] representing the Capture Base attributes with supplementary information from Overlays. When attribute is not found, `null` is returned.
     */
    fun getAttribute(name: String, digest: String): OcaClaimData? {
        return ocaClaimData.firstOrNull { it.captureBaseDigest == digest && it.name == name }
    }

    /**
     * Retrieves all Overlay Bundle attributes.
     *
     * @param digest An optional CESR digest of the associated Capture Base. Default: attributes for all Capture Base digests are considered.
     * @return A list of [OcaClaimData] representing the Capture Base attributes with supplementary information from Overlays.
     */
    fun getAttributes(digest: String? = null): List<OcaClaimData> {
        return if (digest == null) {
            ocaClaimData
        } else {
            ocaClaimData.filter { it.captureBaseDigest == digest }
        }
    }

    /**
     * Retrieves Overlay Bundle attribute associated with their claim source for the data source format.
     *
     * @param dataSourceFormat The format identifier for data source mapping, e.g. "vc+sd-jwt"
     * @param digest An optional CESR digest of the associated Capture Base. Default: attributes for all Capture Base digests are considered.
     * @return A map of ClaimsPathPointer (pointing to data source property) to [OcaClaimData] (for associated Capture Base)
     */
    fun getAttributesForDataSourceFormat(
        dataSourceFormat: DataSourceFormat,
        digest: String?
    ): Map<ClaimsPathPointer, OcaClaimData> {
        return getAttributes(digest = digest)
            .mapNotNull { attribute ->
                val claimSource = attribute.dataSources[dataSourceFormat] ?: return@mapNotNull null
                claimSource to attribute
            }.toMap()
    }
}
