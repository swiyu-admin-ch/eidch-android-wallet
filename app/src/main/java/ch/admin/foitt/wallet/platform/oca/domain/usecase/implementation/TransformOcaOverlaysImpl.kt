package ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointer
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointerComponent
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.toPointerString
import ch.admin.foitt.wallet.platform.oca.domain.model.CaptureBase
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.BrandingOverlay1x1
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.DataSourceOverlay
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.DataSourceOverlay2x0
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.LabelOverlay1x1
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.Overlay
import ch.admin.foitt.wallet.platform.oca.domain.usecase.TransformOcaOverlays
import javax.inject.Inject

class TransformOcaOverlaysImpl @Inject constructor() : TransformOcaOverlays {
    override fun invoke(
        overlays: List<Overlay>,
        captureBases: List<CaptureBase>,
    ): List<Overlay> {
        val dataSourceOverlays = overlays.filterIsInstance<DataSourceOverlay>()

        return overlays.map { overlay ->
            when (overlay) {
                is BrandingOverlay1x1 -> {
                    overlay.copy(
                        primaryField = resolveFieldTemplate(
                            field = overlay.primaryField,
                            defaultDigest = overlay.captureBaseDigest,
                            dataSourceOverlays = dataSourceOverlays
                        ),
                        secondaryField = resolveFieldTemplate(
                            field = overlay.secondaryField,
                            defaultDigest = overlay.captureBaseDigest,
                            dataSourceOverlays = dataSourceOverlays
                        )
                    )
                }

                is LabelOverlay1x1 -> {
                    overlay.copy(
                        attributeLabels = overlay.attributeLabels.mapValues { (_, value) ->
                            resolveFieldTemplate(
                                field = value,
                                defaultDigest = overlay.captureBaseDigest,
                                dataSourceOverlays = dataSourceOverlays
                            ) ?: ""
                        },
                    )
                }

                else -> overlay
            }
        }
    }

    private fun resolveFieldTemplate(
        field: String?,
        defaultDigest: String,
        dataSourceOverlays: List<DataSourceOverlay>
    ): String? {
        return field?.let {
            val regex = Regex(TEMPLATE_REGEX)

            val resolvedField = regex.replace(it) { match ->
                val everything = match.groups["everything"]?.value
                if (everything != null) return@replace ""

                val digest = match.groups["digest"]?.value ?: defaultDigest
                val attribute = match.groups["attribute"]?.value ?: ""
                val index = when (val rawIndex = match.groups["index"]?.value) {
                    null -> null
                    "null" -> ClaimsPathPointerComponent.Null
                    else -> ClaimsPathPointerComponent.Index(rawIndex.toInt())
                }
                val join = match.groups["join"]?.value

                val replacement = getReplacement(
                    dataSourceOverlays = dataSourceOverlays,
                    digest = digest,
                    attribute = attribute,
                    index = index,
                    join = join,
                )

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
        attribute: String,
        index: ClaimsPathPointerComponent?,
        join: String?,
    ): String = getClaimsPathPointer(dataSourceOverlays, digest, attribute)?.let { claimsPathPointer ->
        val path = index?.let { index ->
            val path = if (claimsPathPointer.lastOrNull() is ClaimsPathPointerComponent.Null) {
                claimsPathPointer.dropLast(1)
            } else {
                claimsPathPointer
            }
            path + index
        } ?: claimsPathPointer
        val joinString = join ?: ""
        path.toPointerString() + joinString
    } ?: ""

    private fun getClaimsPathPointer(
        dataSourceOverlays: List<DataSourceOverlay>,
        digest: String,
        attribute: String
    ): ClaimsPathPointer? = dataSourceOverlays.firstNotNullOfOrNull {
        if (it.captureBaseDigest == digest && it is DataSourceOverlay2x0) {
            it.attributeSources[attribute]
        } else {
            null
        }
    }

    private companion object {
        @Suppress("MaximumLineLength")
        const val TEMPLATE_REGEX =
            """\{\{(?:refs:(?<digest>[^:]+):)?(?<attribute>(?:(?!\{\{|\[|\.join\(|\}\}).)*)(?:\[(?<index>\d+|null)\])?(?<join>\.join\((?<q>['"])(?<separator>(?:(?!\k<q>).){0,10})\k<q>\))?\}\}|\{\{(?<everything>.*)?\}\}"""
    }
}
