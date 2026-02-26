package ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.oca.domain.model.AttributeType
import ch.admin.foitt.wallet.platform.oca.domain.model.CaptureBase1x0
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaError
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.BrandingOverlay1x1
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.DataSourceOverlay1x0
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.LabelOverlay1x0
import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaOverlayValidator
import ch.admin.foitt.wallet.platform.oca.mock.OverlayMocks.ocaBundleWithEntryButWithoutEntryCodeOverlay
import ch.admin.foitt.wallet.platform.oca.mock.OverlayMocks.ocaBundleWithEntryKeyNotInEntryCodes
import ch.admin.foitt.wallet.platform.oca.mock.OverlayMocks.ocaBundleWithInvalidOverlayAttributeKey
import ch.admin.foitt.wallet.platform.oca.mock.OverlayMocks.ocaBundleWithInvalidOverlayReferences
import ch.admin.foitt.wallet.platform.oca.mock.OverlayMocks.ocaBundleWithValidEntryAndEntryCodeOverlays
import ch.admin.foitt.wallet.platform.oca.mock.OverlayMocks.ocaBundleWithoutEntryButWithEntryCodeOverlay
import ch.admin.foitt.wallet.platform.oca.mock.OverlayMocks.ocaBundleWithoutEntryButWithMultipleEntryCodeOverlays
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.elfaExample
import ch.admin.foitt.wallet.util.SafeJsonTestInstance
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.annotation.UnsafeResultValueAccess
import io.mockk.MockKAnnotations
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource

class OcaOverlayValidatorImplTest {

    private val json = SafeJsonTestInstance.safeJson

    private lateinit var ocaOverlayValidator: OcaOverlayValidator

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        ocaOverlayValidator = OcaOverlayValidatorImpl()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @OptIn(UnsafeResultValueAccess::class)
    @Test
    fun `Valid overlays are validated successfully`(): Unit = runTest {
        val elfaBundle = json.safeDecodeStringTo<OcaBundle>(elfaExample).value
        ocaOverlayValidator(elfaBundle).assertOk()
    }

    @Test
    fun `Overlays containing invalid references returns an error`(): Unit = runTest {
        ocaOverlayValidator(ocaBundleWithInvalidOverlayReferences).assertErrorType(OcaError.InvalidOverlayCaptureBaseDigest::class)
    }

    @Test
    fun `Overlays containing additional attribute keys does not return an error`(): Unit = runTest {
        ocaOverlayValidator(ocaBundleWithInvalidOverlayAttributeKey).assertOk()
    }

    @ParameterizedTest
    @ValueSource(
        strings = ["en", "xy", "en-US", "en-XY", "xy-XY"]
    )
    fun `Overlays containing valid language codes return Ok`(languageCode: String): Unit = runTest {
        val bundle = getOcaWithLanguageCodeInOverlay(languageCode)
        val result = ocaOverlayValidator(bundle).assertOk()
        assertEquals(bundle.overlays, result)
    }

    @ParameterizedTest
    @ValueSource(
        strings = ["", "x", "è", "en-", "EN", "en-xy", "abcd", "en-xyz", "en-ÜÄ"]
    )
    fun `Overlays containing invalid language codes returns an error`(languageCode: String): Unit = runTest {
        val bundle = getOcaWithLanguageCodeInOverlay(languageCode)

        ocaOverlayValidator(bundle).assertErrorType(OcaError.InvalidOverlayLanguageCode::class)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            // regular expression function
            "$..book[?(@.price <= $['expensive'])]",
            // function extension
            "$..book.length()",
            "$..book.length() ",
            "$..book.length()\t",
            "$..book.length()\n",
            "$.book.concat(foobar)",
            // negative array index
            "$.book[1].foo[-1].bar",
            "$.book[-1]",
            "$.book[-]",
            "$.book[-12345678 ]",
            "$.book[ -1 ]",
            "$.book[ - 1 ]",
            "$[-1]",
            "$.a[-99999999999999999999999999999999]",
            // name cannot start with a digit
            "$.123a",
            """$.x["123a"]""",
            "$['1x']",
            """$["1x"]""",
            // other errors
            """$["x']""",
            "$..y",
            "$..*",
        ]
    )
    fun `DataSourceOverlays containing invalid JsonPaths returns an error`(invalidJsonPath: String) = runTest {
        val bundle = getOcaWithDataSourceOverlay(jsonPath = invalidJsonPath)

        ocaOverlayValidator(bundle).assertErrorType(OcaError.InvalidDataSourceOverlay::class)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "$.x.y",
            "$['x']['y']",
            """$["x"]['y']""",
            "$.x[0]",
            "$.a[99999999999999999999999999999999]",
            "$['x'][0]",
            """$["x"][0]""",
            "$.x[*]",
            "$['x'][*]",
            """$["x"][*]""",
            """$.xyz["x1"]""",
            "$.x['x1']",
            """$.x["x1"]""",
            """$["a"].avb["v123"]"""
        ]
    )
    fun `DataSourceOverlays containing valid JsonPaths returns success`(validJsonPath: String): Unit = runTest {
        val bundle = getOcaWithDataSourceOverlay(jsonPath = validJsonPath)

        ocaOverlayValidator(bundle).assertOk()
    }

    @ParameterizedTest
    @MethodSource("generateInvalidBrandingOverlayImageInputs")
    fun `BrandingOverlays containing invalid uris returns an error`(input: Pair<String, String>) = runTest {
        val bundle = getOcaWithBrandingOverlay(input.first, input.second)

        ocaOverlayValidator(bundle).assertErrorType(OcaError.InvalidBrandingOverlay::class)
    }

    @ParameterizedTest
    @MethodSource("getValidEntryCodeOverlayInputs")
    fun `Valid EntryCodeOverlays are validated successfully`(input: OcaBundle) = runTest {
        ocaOverlayValidator(input).assertOk()
    }

    @ParameterizedTest
    @MethodSource("getValidEntryOverlayInputs")
    fun `Valid EntryOverlays and EntryCodeOverlays are validated successfully`(input: OcaBundle) = runTest {
        ocaOverlayValidator(input).assertOk()
    }

    @ParameterizedTest
    @MethodSource("getInvalidEntryOverlayInputs")
    fun `Invalid Entry and EntryCode overlays return an error`(input: OcaBundle) = runTest {
        ocaOverlayValidator(input).assertErrorType(OcaError.InvalidEntryOverlay::class)
    }

    private fun getOcaWithLanguageCodeInOverlay(languageCode: String) = OcaBundle(
        captureBases = listOf(
            CaptureBase1x0(
                digest = "validDigest",
                attributes = mapOf(
                    "attributeKey" to AttributeType.Text,
                )
            ),
        ),
        overlays = listOf(
            LabelOverlay1x0(
                captureBaseDigest = "validDigest",
                language = languageCode,
                attributeLabels = mapOf(
                    "attributeKey" to "label"
                )
            )
        )
    )

    private fun getOcaWithDataSourceOverlay(jsonPath: String) = OcaBundle(
        captureBases = listOf(
            CaptureBase1x0(
                digest = "validDigest",
                attributes = mapOf(
                    "attributeKey" to AttributeType.Text,
                )
            ),
        ),
        overlays = listOf(
            DataSourceOverlay1x0(
                captureBaseDigest = "validDigest",
                format = "format",
                attributeSources = mapOf("key" to jsonPath)
            )
        )
    )

    private fun getOcaWithBrandingOverlay(logo: String?, backgroundImage: String?) = OcaBundle(
        captureBases = listOf(
            CaptureBase1x0(
                digest = "validDigest",
                attributes = mapOf(
                    "attributeKey" to AttributeType.Text,
                )
            ),
        ),
        overlays = listOf(
            BrandingOverlay1x1(
                captureBaseDigest = "validDigest",
                language = "en",
                logo = logo,
                backgroundImage = backgroundImage,
            )
        )
    )

    companion object {
        @JvmStatic
        fun generateInvalidBrandingOverlayImageInputs() = listOf(
            Pair("invalid", "invalid"),
            Pair("data:image/png;base64,", "invalid"),
            Pair("invalid", "data:image/jpeg;base64,"),
        )

        @JvmStatic
        fun getValidEntryCodeOverlayInputs() = listOf(
            ocaBundleWithoutEntryButWithEntryCodeOverlay,
            ocaBundleWithoutEntryButWithMultipleEntryCodeOverlays,
        )

        @JvmStatic
        fun getValidEntryOverlayInputs() = listOf(
            ocaBundleWithValidEntryAndEntryCodeOverlays,
        )

        @JvmStatic
        fun getInvalidEntryOverlayInputs() = listOf(
            ocaBundleWithEntryButWithoutEntryCodeOverlay,
            ocaBundleWithEntryKeyNotInEntryCodes
        )
    }
}
