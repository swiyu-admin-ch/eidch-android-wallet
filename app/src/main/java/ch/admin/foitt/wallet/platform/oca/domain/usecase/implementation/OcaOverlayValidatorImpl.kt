package ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.toPointerString
import ch.admin.foitt.wallet.platform.oca.domain.model.CaptureBase
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaError
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaOverlayValidationError
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.BrandingOverlay
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.BrandingOverlay1x1
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.DataSourceOverlay
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.DataSourceOverlay2x0
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.EntryCodeOverlay
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.EntryCodeOverlay1x0
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.EntryOverlay
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.EntryOverlay1x0
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.LocalizedOverlay
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.Overlay
import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaOverlayValidator
import ch.admin.foitt.wallet.platform.ssi.domain.model.ImageType
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import javax.inject.Inject

class OcaOverlayValidatorImpl @Inject constructor() : OcaOverlayValidator {
    override suspend fun invoke(
        ocaBundle: OcaBundle,
    ): Result<List<Overlay>, OcaOverlayValidationError> = coroutineBinding {
        val captureBases = ocaBundle.captureBases
        val overlays = ocaBundle.overlays

        if (overlays.containInvalidReferences(captureBases)) {
            return@coroutineBinding Err(OcaError.InvalidOverlayCaptureBaseDigest).bind<List<Overlay>>()
        }

        if (overlays.containInvalidLanguageCodes()) {
            return@coroutineBinding Err(OcaError.InvalidOverlayLanguageCode).bind<List<Overlay>>()
        }

        if (overlays.filterIsInstance<DataSourceOverlay>().isInvalid()) {
            return@coroutineBinding Err(OcaError.InvalidDataSourceOverlay).bind<List<Overlay>>()
        }

        if (overlays.filterIsInstance<BrandingOverlay>().containsInvalidUri()) {
            return@coroutineBinding Err(OcaError.InvalidBrandingOverlay).bind<List<Overlay>>()
        }

        val entryCodeOverlays = overlays.filterIsInstance<EntryCodeOverlay>()
        if (overlays.filterIsInstance<EntryOverlay>().areEntryOverlaysInvalid(captureBases, entryCodeOverlays)) {
            return@coroutineBinding Err(OcaError.InvalidEntryOverlay).bind<List<Overlay>>()
        }

        overlays
    }

    private fun List<Overlay>.containInvalidReferences(captureBases: List<CaptureBase>): Boolean {
        return this.any { overlay ->
            captureBases.none { it.digest == overlay.captureBaseDigest }
        }
    }

    private fun List<Overlay>.containInvalidLanguageCodes(): Boolean {
        return this.filterIsInstance<LocalizedOverlay>().any { overlay ->
            languageRegex.matches(overlay.language).not()
        }
    }

    private fun List<DataSourceOverlay>.isInvalid(): Boolean {
        return this.any { dataSourceOverlay ->
            when (dataSourceOverlay) {
                is DataSourceOverlay2x0 -> dataSourceOverlay.attributeSources.values.any {
                    validClaimsPathPointerRegex.matches(it.toPointerString()).not()
                }
            }
        }
    }

    private fun List<BrandingOverlay>.containsInvalidUri(): Boolean = this.any { brandingOverlay ->
        when (brandingOverlay) {
            is BrandingOverlay1x1 ->
                brandingOverlay.logo?.let { !ImageType.isValidImageDataUri(it) } ?: false ||
                    brandingOverlay.backgroundImage?.let { !ImageType.isValidImageDataUri(it) } ?: false
        }
    }

    private fun List<EntryOverlay>.areEntryOverlaysInvalid(
        captureBases: List<CaptureBase>,
        entryCodeOverlays: List<EntryCodeOverlay>,
    ) = captureBases.any { captureBase ->
        val entryCodeOverlaysForCaptureBase = entryCodeOverlays.filter { it.captureBaseDigest == captureBase.digest }
        val entryOverlaysForCaptureBase = this.filter { it.captureBaseDigest == captureBase.digest }

        if (entryCodeOverlaysForCaptureBase.isEmpty()) {
            // entry overlays also empty -> not invalid
            // entry overlays not empty -> validation will always fail
            return@any entryOverlaysForCaptureBase.isNotEmpty()
        }

        val entryCodeOverlay = entryCodeOverlaysForCaptureBase.first()
        val attributeEntryCodes = getAttributeEntryCodes(entryCodeOverlay)

        val entryValuesNotInEntryCode = entryOverlaysForCaptureBase.any { entryOverlay ->
            when (entryOverlay) {
                is EntryOverlay1x0 -> {
                    entryOverlay.attributeEntries.any { attributeEntries ->
                        val codes = attributeEntryCodes[attributeEntries.key] ?: emptyList()
                        !codes.containsAll(attributeEntries.value.keys)
                    }
                }
            }
        }

        entryValuesNotInEntryCode
    }

    private fun getAttributeEntryCodes(entryCodeOverlay: EntryCodeOverlay) = when (entryCodeOverlay) {
        is EntryCodeOverlay1x0 -> entryCodeOverlay.attributeEntryCodes
    }

    private companion object {
        val languageRegex = Regex("^[a-z]{2}(-[A-Z]{2})?$")

        // Matches claims path pointers
        // start with: [, end with: ]
        // contains: strings, integers, null (separated by commas)
        val validClaimsPathPointerRegex = Regex(
            """^\s*\[\s*(?:"[^"]+"|\d+|null)(?:\s*,\s*(?:"[^"]+"|\d+|null))*\s*]\s*$"""
        )
    }
}
