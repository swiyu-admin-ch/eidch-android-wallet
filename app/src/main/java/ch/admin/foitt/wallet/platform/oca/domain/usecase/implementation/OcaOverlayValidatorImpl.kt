package ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.toPointerString
import ch.admin.foitt.wallet.platform.oca.domain.model.CaptureBase
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaError
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaOverlayValidationError
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.BrandingOverlay
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.BrandingOverlay1x1
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.DataSourceOverlay
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.DataSourceOverlay1x0
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
                is DataSourceOverlay1x0 -> dataSourceOverlay.attributeSources.values.any {
                    validJsonPathRegex.matches(it).not()
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

        // Matches a root identifier `$` followed by one or more children in dot or bracket notation, e.g. e.g. `$.a`, `$["a"]`, `$['a']`
        // Arrays with a non-negative integer index or wildcards are accepted, e.g. `$.x[0]` or `$.x[*]`
        // See https://github.com/e-id-admin/open-source-community/blob/main/tech-roadmap/rfcs/oca/spec.md#jsonpath-consideration for
        // specification and tests for examples of valid and invalid json paths
        val validJsonPathRegex = Regex(
            """(?x) # allow whitespace in regex
            ^\$( # start with $ followed by one or more of dot notations, bracket notations or arrays
                (?<dot>\.([a-zA-Z_]\w*|\*)) | # dot notation: a dot and a name or an asterisk, e.g. .foo or .*
                (?<bracket>\[(\*|(?<quote>["'])([a-zA-Z_]\w*)\k<quote>)]) | # bracket notation: a name in either single or double quotes or an asterisk in brackets, e.g. ["foo"] or [*]
                (?<array>\[\d+]) # array: positive number in brackets, e.g [1] or [1234567]
              )+$
            """.trimMargin(),
            RegexOption.COMMENTS
        )

        // Matches claims path pointers
        // start with: [, end with: ]
        // contains: strings, integers, null (separated by commas)
        val validClaimsPathPointerRegex = Regex(
            """^\s*\[\s*(?:"[^"]+"|\d+|null)(?:\s*,\s*(?:"[^"]+"|\d+|null))*\s*]\s*$"""
        )
    }
}
