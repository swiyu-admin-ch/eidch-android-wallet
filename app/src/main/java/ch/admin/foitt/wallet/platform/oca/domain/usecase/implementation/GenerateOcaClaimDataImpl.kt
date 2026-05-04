package ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointer
import ch.admin.foitt.wallet.platform.oca.domain.model.AttributeKey
import ch.admin.foitt.wallet.platform.oca.domain.model.CaptureBase
import ch.admin.foitt.wallet.platform.oca.domain.model.DataSourceFormat
import ch.admin.foitt.wallet.platform.oca.domain.model.EntryCode
import ch.admin.foitt.wallet.platform.oca.domain.model.Locale
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaClaimData
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.CharacterEncoding
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.CharacterEncodingOverlay
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.CharacterEncodingOverlay1x0
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.DataSourceOverlay
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.DataSourceOverlay1x0
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.DataSourceOverlay2x0
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.EntryOverlay
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.EntryOverlay1x0
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.FormatOverlay
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.FormatOverlay1x0
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.LabelOverlay
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.LabelOverlay1x0
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.OrderOverlay
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.OrderOverlay1x0
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.Overlay
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.SensitiveOverlay
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.SensitiveOverlay1x0
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.Standard
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.StandardOverlay
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.StandardOverlay1x0
import ch.admin.foitt.wallet.platform.oca.domain.usecase.GenerateOcaClaimData
import ch.admin.foitt.wallet.platform.oca.domain.util.getLatestOverlaysOfType
import ch.admin.foitt.wallet.platform.oca.domain.util.naiveJsonPathToClaimsPathPointer
import ch.admin.foitt.wallet.platform.utils.associateNotNull
import ch.admin.foitt.wallet.platform.utils.associateWithNotNull
import timber.log.Timber
import javax.inject.Inject

class GenerateOcaClaimDataImpl @Inject constructor() : GenerateOcaClaimData {
    override fun invoke(
        captureBases: List<CaptureBase>,
        overlays: List<Overlay>
    ) = captureBases.flatMap { captureBase ->
        val characterEncodings = getEncodingsForAttributes(overlays, captureBase)
        val dataSources = getDataSourcesForAttributes(overlays, captureBase)
        val formats = getFormatsForAttributes(overlays, captureBase)
        val labelOverlays = getLabelsForAttributes(overlays, captureBase)
        val standards = getStandardsForAttributes(overlays, captureBase)
        val order = getOrderForAttributes(overlays, captureBase)
        val entries = getEntryMappingForAttributes(overlays, captureBase)
        val sensitiveAttributes = getSensitiveAttributes(overlays, captureBase)
        captureBase.attributes.map { attribute ->
            OcaClaimData(
                attributeType = attribute.value,
                captureBaseDigest = captureBase.digest,
                name = attribute.key,
                characterEncoding = characterEncodings.getOrDefault(attribute.key, null),
                dataSources = dataSources.getOrDefault(attribute.key, emptyMap()),
                format = formats.getOrDefault(attribute.key, null),
                labels = labelOverlays.getOrDefault(attribute.key, emptyMap()),
                standard = standards.getOrDefault(attribute.key, null),
                order = order.getOrDefault(attribute.key, null),
                entryMappings = entries.getOrDefault(attribute.key, emptyMap()),
                isSensitive = sensitiveAttributes.contains(attribute.key)
            )
        }
    }

    private fun getLabelsForAttributes(overlays: List<Overlay>, captureBase: CaptureBase): Map<AttributeKey, Map<Locale, String>> {
        val labelOverlays = getLatestOverlaysOfType<LabelOverlay>(overlays = overlays, digest = captureBase.digest)
        val labels = captureBase.attributes.keys.associateWith { attribute ->
            labelOverlays.mapNotNull { overlay ->
                when (overlay) {
                    is LabelOverlay1x0 -> {
                        overlay.attributeLabels[attribute]?.let {
                            overlay.language to it
                        }
                    }
                }
            }.toMap()
        }

        if (labels.any { labelOverlays.size > it.value.size }) {
            Timber.w("Duplicate in label overlays")
        }

        return labels
    }

    private fun getSensitiveAttributes(overlays: List<Overlay>, captureBase: CaptureBase): List<AttributeKey> {
        val sensitiveOverlays =
            getLatestOverlaysOfType<SensitiveOverlay>(overlays = overlays, digest = captureBase.digest)

        return captureBase.attributes.keys.filter { attribute ->
            sensitiveOverlays.map { overlay ->
                when (overlay) {
                    is SensitiveOverlay1x0 -> overlay.attributes.contains(attribute)
                }
            }.any { it }
        }
    }

    private fun getDataSourcesForAttributes(
        overlays: List<Overlay>,
        captureBase: CaptureBase,
    ): Map<AttributeKey, Map<DataSourceFormat, ClaimsPathPointer>> {
        val dataSourceOverlays =
            getLatestOverlaysOfType<DataSourceOverlay>(overlays = overlays, digest = captureBase.digest)
        val dataSources = captureBase.attributes.keys.associateWith { attribute ->
            dataSourceOverlays.mapNotNull { overlay ->
                when (overlay) {
                    is DataSourceOverlay2x0 -> {
                        overlay.attributeSources[attribute]?.let { claimsPathPointer ->
                            overlay.format to claimsPathPointer
                        }
                    }

                    is DataSourceOverlay1x0 -> {
                        overlay.attributeSources[attribute]?.let { jsonPathString ->
                            overlay.format to naiveJsonPathToClaimsPathPointer(jsonPathString)
                        }
                    }
                }
            }.toMap()
        }

        if (dataSources.any { dataSourceOverlays.size > it.value.size }) {
            Timber.w("Duplicate in data source overlays")
        }

        return dataSources
    }

    private fun getFormatsForAttributes(overlays: List<Overlay>, captureBase: CaptureBase): Map<AttributeKey, String> {
        val formatOverlays = getLatestOverlaysOfType<FormatOverlay>(overlays = overlays, digest = captureBase.digest)
        val overlay = formatOverlays.firstOrNull() ?: return emptyMap()
        return captureBase.attributes.keys.associateWithNotNull { attribute ->
            when (overlay) {
                is FormatOverlay1x0 -> overlay.attributeFormats[attribute]
            }
        }
    }

    private fun getStandardsForAttributes(overlays: List<Overlay>, captureBase: CaptureBase): Map<AttributeKey, Standard> {
        val standardOverlays =
            getLatestOverlaysOfType<StandardOverlay>(overlays = overlays, digest = captureBase.digest)
        val overlay = standardOverlays.firstOrNull() ?: return emptyMap()
        return captureBase.attributes.keys.associateWithNotNull { attribute ->
            when (overlay) {
                is StandardOverlay1x0 -> Standard.fromString(overlay.attributeStandards[attribute])
            }
        }
    }

    private fun getEncodingsForAttributes(overlays: List<Overlay>, captureBase: CaptureBase): Map<AttributeKey, CharacterEncoding> {
        val encodingOverlays =
            getLatestOverlaysOfType<CharacterEncodingOverlay>(overlays = overlays, digest = captureBase.digest)
        val overlay = encodingOverlays.firstOrNull() ?: return emptyMap()
        return captureBase.attributes.keys.associateWithNotNull { attribute ->
            when (overlay) {
                is CharacterEncodingOverlay1x0 -> {
                    CharacterEncoding.fromString(overlay.attributeCharacterEncoding[attribute])
                        ?: CharacterEncoding.fromString(overlay.defaultCharacterEncoding)
                }
            }
        }
    }

    private fun getOrderForAttributes(overlays: List<Overlay>, captureBase: CaptureBase): Map<AttributeKey, Int> {
        val orderOverlays = getLatestOverlaysOfType<OrderOverlay>(overlays = overlays, digest = captureBase.digest)
        val overlay = orderOverlays.firstOrNull() ?: return emptyMap()
        return captureBase.attributes.keys.associateWithNotNull { attribute ->
            when (overlay) {
                is OrderOverlay1x0 -> overlay.attributeOrders[attribute]
            }
        }
    }

    private fun getEntryMappingForAttributes(
        overlays: List<Overlay>,
        captureBase: CaptureBase
    ): Map<AttributeKey, Map<Locale, Map<EntryCode, String>>> {
        val entryOverlays = getLatestOverlaysOfType<EntryOverlay>(overlays = overlays, digest = captureBase.digest)
        val attributes = captureBase.attributes.map { it.key }
        val entries = attributes.associateWithNotNull { attribute ->
            entryOverlays.associateNotNull { overlay ->
                when (overlay) {
                    is EntryOverlay1x0 -> overlay.language to overlay.attributeEntries[attribute]
                }
            }
        }
        return entries
    }
}
