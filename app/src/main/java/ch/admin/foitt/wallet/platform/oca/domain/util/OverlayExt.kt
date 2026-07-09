package ch.admin.foitt.wallet.platform.oca.domain.util

import ch.admin.foitt.wallet.platform.oca.domain.model.OverlayVersion
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.LabelOverlay
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.Overlay

/**
 * Retrieves Overlays for latest version by type and digest.
 * - Parameters:
 * - Overlay: The specific Overlay to look for. Must be a specific Overlay interface or class, e.g. [LabelOverlay] or [LabelOverlay1x0].
 * - digest: An optional CESR digest of the associated Capture Base. Default: All Capture Bases
 * - Returns: The list of matching [Overlay]s.
 */
internal inline fun <reified T : Overlay> getLatestOverlaysOfType(overlays: List<Overlay>, digest: String? = null): List<T> {
    val overlaysOfType = overlays.filterIsInstance<T>()
    val latestOverlayType = overlaysOfType
        .map { it.type }
        .distinct()
        .maxByOrNull { specType ->
            val versionString = specType.type.split("/").last() // e.g., "1.2.3"
            OverlayVersion(versionString)
        }

    return overlaysOfType.filter { overlay ->
        overlay.type == latestOverlayType
    }.let { filteredOverlays ->
        digest?.let { digest ->
            filteredOverlays.filter { digest == it.captureBaseDigest }
        } ?: filteredOverlays
    }
}
