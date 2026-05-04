package ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimWithDisplays
import ch.admin.foitt.wallet.platform.locale.LocaleCompat
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetCurrentAppLocale
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimImage
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimText
import ch.admin.foitt.wallet.platform.ssi.domain.model.MapToCredentialClaimDataError
import ch.admin.foitt.wallet.platform.ssi.domain.model.ValueType
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.MapToCredentialClaimData
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock.MockCredentialClaim
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock.MockCredentialClaim.CLAIM_ID
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock.MockCredentialClaim.buildClaimWithDisplays
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock.MockCredentialClaim.credentialClaimDisplay
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock.MockCredentialClaim.credentialClaimDisplays
import ch.admin.foitt.wallet.platform.utils.base64NonUrlStringToByteArray
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.util.Locale

class MapToCredentialClaimDataImplTest {

    private lateinit var mapToCredentialClaimData: MapToCredentialClaimData

    @MockK
    private lateinit var mockGetLocalizedDisplay: GetLocalizedDisplay

    @MockK
    private lateinit var mockGetCurrentAppLocale: GetCurrentAppLocale

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        mockkStatic("ch.admin.foitt.wallet.platform.utils.StringUtilsKt")
        every { base64NonUrlStringToByteArray(any()) } returns byteArrayOf()

        mockkStatic("android.text.format.DateFormat")

        mapToCredentialClaimData = MapToCredentialClaimDataImpl(
            getLocalizedDisplay = mockGetLocalizedDisplay,
            getCurrentAppLocale = mockGetCurrentAppLocale
        )

        coEvery { mockGetLocalizedDisplay(displays = credentialClaimDisplays) } returns credentialClaimDisplay
        coEvery { mockGetCurrentAppLocale() } returns Locale.ENGLISH
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "bool",
            "datetime",
            "string"
        ]
    )
    fun `Claim with supported string type should return CredentialClaimText`(valueTypeString: String) = runTest {
        val claimWithDisplays = buildClaimWithDisplays(valueTypeString)

        val data = mapToCredentialClaimData(claimWithDisplays).assertOk()
        assertTrue(data is CredentialClaimText, "string valueType should return ${CredentialClaimText::class.simpleName}")
        assertEquals(credentialClaimDisplay.name, data.localizedLabel)
        assertEquals(claimWithDisplays.claim.value, (data as CredentialClaimText).value)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "bool",
            "datetime",
            "image/png",
            "image/jpeg",
            "numeric",
            "string"
        ]
    )
    fun `Claim with null value should return CredentialClaimText`(valueTypeString: String) = runTest {
        val claimWithDisplays = buildClaimWithDisplays(value = null, valueType = valueTypeString)

        val data = mapToCredentialClaimData(claimWithDisplays).assertOk()

        assertTrue(data is CredentialClaimText)
        val credentialClaimText = data as CredentialClaimText
        assertEquals(credentialClaimDisplay.name, credentialClaimText.localizedLabel)
        assertEquals(null, credentialClaimText.value)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "bool",
            "string",
            "numeric"
        ]
    )
    fun `Claim where display contains a value returns display data containing the display value`(claimValueType: String) = runTest {
        val displayValue = "displayValue"
        val display = CredentialClaimDisplay(
            claimId = CLAIM_ID,
            name = "name",
            locale = "xxx",
            value = displayValue,
        )
        val displays = listOf(display)
        val claimWithDisplays = buildClaimWithDisplays(valueType = claimValueType, displays = displays)

        coEvery { mockGetLocalizedDisplay(displays = displays) } returns display

        val data = mapToCredentialClaimData(claimWithDisplays).assertOk()

        assertTrue(data is CredentialClaimText, "$claimValueType valueType should return ${CredentialClaimText::class.simpleName}")
        assertEquals(displayValue, (data as CredentialClaimText).value)
    }

    @ParameterizedTest
    @MethodSource("generateNumericClaimMappings")
    fun `Claim with value type 'numeric' returns localized CredentialClaimText`(input: Triple<String, String, String>) = runTest {
        val localeParts = input.second.split("-")
        val preferredLocale = when (localeParts.size) {
            1 -> LocaleCompat.of(localeParts[0])
            2 -> LocaleCompat.of(localeParts[0], localeParts[1])
            else -> Locale.getDefault()
        }
        coEvery { mockGetCurrentAppLocale() } returns preferredLocale

        val display = CredentialClaimDisplay(
            claimId = 1,
            name = "name",
            locale = input.second,
            value = null,
        )
        val displays = listOf(display)
        val claimWithDisplays = CredentialClaimWithDisplays(
            claim = CredentialClaim(
                id = 1,
                clusterId = 1,
                path = "key",
                value = input.first,
                valueType = ValueType.NUMERIC.value
            ),
            displays = displays,
        )

        coEvery { mockGetLocalizedDisplay(displays = displays) } returns display

        val data = mapToCredentialClaimData(claimWithDisplays).assertOk()
        assertTrue(data is CredentialClaimText)
        assertEquals(input.third, (data as CredentialClaimText).value)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "image/png",
            "image/jpeg",
        ]
    )
    fun `Claim with supported image mime type should return correct data`(imageMimeType: String) = runTest {
        val claimWithDisplays = buildClaimWithDisplays(imageMimeType)

        val data = mapToCredentialClaimData(claimWithDisplays).assertOk()
        assertTrue(data is CredentialClaimImage, "$imageMimeType mime type should return ${CredentialClaimImage::class.simpleName}")
        assertEquals(credentialClaimDisplay.name, data.localizedLabel)
        assertEquals(base64NonUrlStringToByteArray(claimWithDisplays.claim.value!!), (data as CredentialClaimImage).imageData)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "boolean",
            "image",
            "image/jp",
            "image/jp2",
            "image/jpg",
            "imageDataUri",
        ]
    )
    fun `Claim with invalid valueType should return an error`(valueTypeString: String) = runTest {
        mapToCredentialClaimData(buildClaimWithDisplays(valueTypeString))
            .assertErrorType(MapToCredentialClaimDataError::class)
    }

    @Test
    fun `Claim with null valueType should return an error`() = runTest {
        mapToCredentialClaimData(buildClaimWithDisplays("null"))
            .assertErrorType(MapToCredentialClaimDataError::class)
    }

    @Test
    fun `Claim with empty valueType should return an error`() = runTest {
        mapToCredentialClaimData(buildClaimWithDisplays(""))
            .assertErrorType(MapToCredentialClaimDataError::class)
    }

    @Test
    fun `Claim with string valueType but no displays should return an error`() = runTest {
        coEvery { mockGetLocalizedDisplay(displays = any<List<CredentialClaimDisplay>>()) } returns null
        val claimWithDisplays = buildClaimWithDisplays("string", displays = emptyList())

        mapToCredentialClaimData(claimWithDisplays)
            .assertErrorType(MapToCredentialClaimDataError::class)
    }

    @Test
    fun `Claim with long value is truncated`() = runTest {
        val claimWithDisplays =
            buildClaimWithDisplays(valueType = "string", value = MockCredentialClaim.LONG_CLAIM_VALUE)

        val data = mapToCredentialClaimData(claimWithDisplays).assertOk()
        val expected = MockCredentialClaim.LONG_CLAIM_VALUE.substring(0, 1800) + "…"
        assertTrue(data is CredentialClaimText)
        assertEquals(expected, (data as CredentialClaimText).value)
    }

    companion object {
        private const val NNBSP = "\u202F" // languages like f. e. french use a narrow non-breaking space between thousands

        @JvmStatic
        fun generateNumericClaimMappings() = listOf(
            // valid inputs
            // small integer
            Triple("12", "de-CH", "12"),
            Triple("12", "rm", "12"),
            Triple("12", "de", "12"),
            Triple("12", "it", "12"),
            Triple("12", "en", "12"),
            Triple("12", "fr", "12"),

            // large integer
            Triple("12345678", "de-CH", "12’345’678"),
            Triple("12345678", "rm", "12’345’678"),
            Triple("12345678", "de", "12.345.678"),
            Triple("12345678", "it", "12.345.678"),
            Triple("12345678", "en", "12,345,678"),
            Triple("12345678", "fr", "12${NNBSP}345${NNBSP}678"),

            Triple("-12345678", "de-CH", "-12’345’678"),
            Triple("-12345678", "rm", "-12’345’678"),
            Triple("-12345678", "de", "-12.345.678"),
            Triple("-12345678", "it", "-12.345.678"),
            Triple("-12345678", "en", "-12,345,678"),
            Triple("-12345678", "fr", "-12${NNBSP}345${NNBSP}678"),

            // integer with exponent
            Triple("12345678E+3", "de-CH", "12’345’678E+3"),
            Triple("12345678E+3", "rm", "12’345’678E+3"),
            Triple("12345678E+3", "de", "12.345.678E+3"),
            Triple("12345678E+3", "it", "12.345.678E+3"),
            Triple("12345678E+3", "en", "12,345,678E+3"),
            Triple("12345678E+3", "fr", "12${NNBSP}345${NNBSP}678E+3"),

            // decimals
            Triple("1234.5678", "de-CH", "1’234.5678"),
            Triple("1234.5678", "rm", "1’234.5678"),
            Triple("1234.5678", "de", "1.234,5678"),
            Triple("1234.5678", "it", "1.234,5678"),
            Triple("1234.5678", "en", "1,234.5678"),
            Triple("1234.5678", "fr", "1${NNBSP}234,5678"),

            Triple("-1234.5678", "de-CH", "-1’234.5678"),
            Triple("-1234.5678", "rm", "-1’234.5678"),
            Triple("-1234.5678", "de", "-1.234,5678"),
            Triple("-1234.5678", "it", "-1.234,5678"),
            Triple("-1234.5678", "en", "-1,234.5678"),
            Triple("-1234.5678", "fr", "-1${NNBSP}234,5678"),

            // decimals with exponent
            Triple("1234.5678E+3", "de-CH", "1’234.5678E+3"),
            Triple("1234.5678E+3", "rm", "1’234.5678E+3"),
            Triple("1234.5678E+3", "de", "1.234,5678E+3"),
            Triple("1234.5678E+3", "it", "1.234,5678E+3"),
            Triple("1234.5678E+3", "en", "1,234.5678E+3"),
            Triple("1234.5678E+3", "fr", "1${NNBSP}234,5678E+3"),

            Triple("-1234.5678E+3", "de-CH", "-1’234.5678E+3"),
            Triple("-1234.5678E+3", "rm", "-1’234.5678E+3"),
            Triple("-1234.5678E+3", "de", "-1.234,5678E+3"),
            Triple("-1234.5678E+3", "it", "-1.234,5678E+3"),
            Triple("-1234.5678E+3", "en", "-1,234.5678E+3"),
            Triple("-1234.5678E+3", "fr", "-1${NNBSP}234,5678E+3"),

            // exponents (uppercase/lowercase E, followed by optional plus/minus sign, followed by integer)
            Triple("1234E+3", "de-CH", "1’234E+3"),
            Triple("1234E+3", "rm", "1’234E+3"),
            Triple("1234E+3", "de", "1.234E+3"),
            Triple("1234E+3", "it", "1.234E+3"),
            Triple("1234E+3", "en", "1,234E+3"),
            Triple("1234E+3", "fr", "1${NNBSP}234E+3"),

            Triple("1234E-3", "de-CH", "1’234E-3"),
            Triple("1234E-3", "rm", "1’234E-3"),
            Triple("1234E-3", "de", "1.234E-3"),
            Triple("1234E-3", "it", "1.234E-3"),
            Triple("1234E-3", "en", "1,234E-3"),
            Triple("1234E-3", "fr", "1${NNBSP}234E-3"),

            Triple("1234E3", "de-CH", "1’234E3"),
            Triple("1234E3", "rm", "1’234E3"),
            Triple("1234E3", "de", "1.234E3"),
            Triple("1234E3", "it", "1.234E3"),
            Triple("1234E3", "en", "1,234E3"),
            Triple("1234E3", "fr", "1${NNBSP}234E3"),

            Triple("1234e+3", "de-CH", "1’234e+3"),
            Triple("1234e+3", "rm", "1’234e+3"),
            Triple("1234e+3", "de", "1.234e+3"),
            Triple("1234e+3", "it", "1.234e+3"),
            Triple("1234e+3", "en", "1,234e+3"),
            Triple("1234e+3", "fr", "1${NNBSP}234e+3"),

            Triple("1234e-3", "de-CH", "1’234e-3"),
            Triple("1234e-3", "rm", "1’234e-3"),
            Triple("1234e-3", "de", "1.234e-3"),
            Triple("1234e-3", "it", "1.234e-3"),
            Triple("1234e-3", "en", "1,234e-3"),
            Triple("1234e-3", "fr", "1${NNBSP}234e-3"),

            Triple("1234e3", "de-CH", "1’234e3"),
            Triple("1234e3", "rm", "1’234e3"),
            Triple("1234e3", "de", "1.234e3"),
            Triple("1234e3", "it", "1.234e3"),
            Triple("1234e3", "en", "1,234e3"),
            Triple("1234e3", "fr", "1${NNBSP}234e3"),

            // invalid inputs
            // missing integer part
            Triple(".1234E-3", "de-CH", ".1234E-3"),
            Triple(".1234E-3", "rm", ".1234E-3"),
            Triple(".1234E-3", "de", ".1234E-3"),
            Triple(".1234E-3", "it", ".1234E-3"),
            Triple(".1234E-3", "en", ".1234E-3"),
            Triple(".1234E-3", "fr", ".1234E-3"),

            Triple("-.1234E-3", "de-CH", "-.1234E-3"),
            Triple("-.1234E-3", "rm", "-.1234E-3"),
            Triple("-.1234E-3", "de", "-.1234E-3"),
            Triple("-.1234E-3", "it", "-.1234E-3"),
            Triple("-.1234E-3", "en", "-.1234E-3"),
            Triple("-.1234E-3", "fr", "-.1234E-3"),

            // invalid exponent part
            Triple("1234E", "de-CH", "1234E"),
            Triple("1234E", "rm", "1234E"),
            Triple("1234E", "de", "1234E"),
            Triple("1234E", "it", "1234E"),
            Triple("1234E", "en", "1234E"),
            Triple("1234E", "fr", "1234E"),

            Triple("1234E-", "de-CH", "1234E-"),
            Triple("1234E-", "rm", "1234E-"),
            Triple("1234E-", "de", "1234E-"),
            Triple("1234E-", "it", "1234E-"),
            Triple("1234E-", "en", "1234E-"),
            Triple("1234E-", "fr", "1234E-"),

            Triple("1234-3", "de-CH", "1234-3"),
            Triple("1234-3", "rm", "1234-3"),
            Triple("1234-3", "de", "1234-3"),
            Triple("1234-3", "it", "1234-3"),
            Triple("1234-3", "en", "1234-3"),
            Triple("1234-3", "fr", "1234-3"),

            // currencies
            Triple("1000€", "de", "1000€"),
            Triple("$1000", "en", "$1000"),

            // not a number
            Triple("invalidInput", "de-CH", "invalidInput"),
            Triple("invalidInput", "rm", "invalidInput"),
            Triple("invalidInput", "de", "invalidInput"),
            Triple("invalidInput", "it", "invalidInput"),
            Triple("invalidInput", "en", "invalidInput"),
            Triple("invalidInput", "fr", "invalidInput"),
        )
    }
}
