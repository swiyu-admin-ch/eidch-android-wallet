package ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.toPointerString
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.BrandingOverlay1x1
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.DataSourceOverlay
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.DataSourceOverlay1x0
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.DataSourceOverlay2x0
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.Overlay
import ch.admin.foitt.wallet.platform.oca.domain.usecase.TransformOcaOverlays
import javax.inject.Inject

class TransformOcaOverlaysImpl @Inject constructor() : TransformOcaOverlays {
    override fun invoke(overlays: List<Overlay>): List<Overlay> {
        return overlays.resolveBrandingOverlay()
    }

    private fun List<Overlay>.resolveBrandingOverlay(): List<Overlay> {
        val dataSourceOverlays = this.filterIsInstance<DataSourceOverlay>()

        return this.map { overlay ->
            when (overlay) {
                is BrandingOverlay1x1 -> {
                    overlay.copy(
                        primaryField = resolveFieldTemplate(overlay.captureBaseDigest, overlay.primaryField, dataSourceOverlays),
                        secondaryField = resolveFieldTemplate(overlay.captureBaseDigest, overlay.secondaryField, dataSourceOverlays)
                    )
                }
                else -> overlay
            }
        }
    }

    private fun resolveFieldTemplate(brandingDigest: String, field: String?, dataSourceOverlays: List<DataSourceOverlay>): String? {
        return field?.let {
            // matches: 2 groups
            // 1: digest -> if not null -> reference attribute
            // 2: anything else -> {{anything}}
            val bracketRegex = Regex("""\{\{(?:refs:([\w-]+):)?(.+?)\}\}""")

            val resolvedField = bracketRegex.replace(it) { match ->
                val digest = match.groups[1]?.value ?: brandingDigest
                val attribute = match.groups[2]?.value ?: ""

                val replacement = getReplacement(dataSourceOverlays, digest, attribute)

                if (replacement.isNotEmpty()) {
                    "{{$replacement}}"
                } else {
                    replacement
                }
            }

            resolvedField
        }
    }

    private fun getReplacement(
        dataSourceOverlays: List<DataSourceOverlay>,
        digest: String,
        attribute: String
    ) = dataSourceOverlays
        .find { it.captureBaseDigest == digest }
        ?.let { dataSourceOverlay ->
            when (dataSourceOverlay) {
                is DataSourceOverlay2x0 -> dataSourceOverlay.attributeSources[attribute]?.toPointerString() ?: ""
                is DataSourceOverlay1x0 -> dataSourceOverlay.attributeSources[attribute] ?: ""
            }
        } ?: ""
}
