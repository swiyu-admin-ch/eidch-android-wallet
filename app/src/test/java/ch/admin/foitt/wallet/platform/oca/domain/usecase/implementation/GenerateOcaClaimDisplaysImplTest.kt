package ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.credential.domain.model.AnyClaimDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage
import ch.admin.foitt.wallet.platform.imageValidation.domain.model.ImageValidationError
import ch.admin.foitt.wallet.platform.imageValidation.domain.usecase.ValidateImage
import ch.admin.foitt.wallet.platform.oca.domain.model.AttributeType
import ch.admin.foitt.wallet.platform.oca.domain.model.DateTimePattern
import ch.admin.foitt.wallet.platform.oca.domain.model.EntryCode
import ch.admin.foitt.wallet.platform.oca.domain.model.Locale
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaClaimData
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaError
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.CharacterEncoding
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.Standard
import ch.admin.foitt.wallet.platform.oca.domain.usecase.GenerateOcaClaimDisplays
import ch.admin.foitt.wallet.platform.ssi.domain.model.ImageType
import ch.admin.foitt.wallet.platform.ssi.domain.model.ValueType
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class GenerateOcaClaimDisplaysImplTest {

    @MockK
    private lateinit var mockValidateImage: ValidateImage
    private lateinit var useCase: GenerateOcaClaimDisplays

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        useCase = GenerateOcaClaimDisplaysImpl(mockValidateImage)

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    private fun setupDefaultMocks() {
        every { mockValidateImage(any(), any()) } returns Ok(Unit)
    }

    @Test
    fun `Generating Oca claim displays with labels returns claim with displays`() = runTest {
        val labels = createLabels(listOf("en", "de"))
        val ocaClaimData = createOcaClaimData(AttributeType.Text, labels = labels)

        val result = useCase(CLAIM_KEY, CLAIM_VALUE, ocaClaimData).assertOk()

        val expectedDisplays = listOf(
            AnyClaimDisplay("en", "${LABEL}_en", null),
            AnyClaimDisplay("de", "${LABEL}_de", null),
            fallbackClaimDisplay,
        )
        assertClaim(result.first)
        assertEquals(expectedDisplays, result.second)
    }

    @Test
    fun `Generating Oca claim displays with entries returns claim with displays`() = runTest {
        val entryMappings = createEntryMappings(listOf("en", "de"))
        val ocaClaimData = createOcaClaimData(AttributeType.Text, entryMappings = entryMappings)

        val result = useCase(CLAIM_KEY, ENTRY_CODE, ocaClaimData).assertOk()

        val expectedDisplays = listOf(
            fallbackClaimDisplay,
            AnyClaimDisplay("en", CLAIM_KEY, "${ENTRY_CODE}_en"),
            AnyClaimDisplay("de", CLAIM_KEY, "${ENTRY_CODE}_de"),
        )
        assertClaim(result.first, value = ENTRY_CODE)
        assertEquals(expectedDisplays, result.second)
    }

    @Test
    fun `Generating Oca claim displays with labels and entries returns claim with displays`() = runTest {
        val labels = createLabels(listOf("en", "de"))
        val entryMappings = createEntryMappings(listOf("en", "de"))
        val ocaClaimData = createOcaClaimData(AttributeType.Text, entryMappings = entryMappings, labels = labels)

        val result = useCase(CLAIM_KEY, ENTRY_CODE, ocaClaimData).assertOk()

        val expectedDisplays = listOf(
            AnyClaimDisplay("en", "${LABEL}_en", "${ENTRY_CODE}_en"),
            AnyClaimDisplay("de", "${LABEL}_de", "${ENTRY_CODE}_de"),
            fallbackClaimDisplay,
        )
        assertClaim(result.first, value = ENTRY_CODE)
        assertEquals(expectedDisplays, result.second)
    }

    @Test
    fun `Generating Oca claim displays with some labels and some entries returns claim with displays`() = runTest {
        val labels = createLabels(listOf("en", "de"))
        val entryMappings = createEntryMappings(listOf("de", "fr"))
        val ocaClaimData = createOcaClaimData(AttributeType.Text, entryMappings = entryMappings, labels = labels)

        val result = useCase(CLAIM_KEY, ENTRY_CODE, ocaClaimData).assertOk()

        val expectedDisplays = listOf(
            AnyClaimDisplay("en", "${LABEL}_en", null),
            AnyClaimDisplay("de", "${LABEL}_de", "${ENTRY_CODE}_de"),
            fallbackClaimDisplay,
            AnyClaimDisplay("fr", CLAIM_KEY, "${ENTRY_CODE}_fr"),
        )
        assertClaim(result.first, value = ENTRY_CODE)
        assertEquals(expectedDisplays, result.second)
    }

    @Test
    fun `Generating Oca claim displays without labels nor entries returns claims with fallback display only`() = runTest {
        val ocaClaimData = createOcaClaimData(AttributeType.Text)

        val result = useCase(CLAIM_KEY, CLAIM_VALUE, ocaClaimData).assertOk()

        assertClaim(result.first)
        assertEquals(listOf(fallbackClaimDisplay), result.second)
    }

    @Test
    fun `Generating Oca claim displays with mismatching entries returns claims with fallback display only`() = runTest {
        val entryMappings = createEntryMappings(listOf("de"), "other")
        val ocaClaimData = createOcaClaimData(AttributeType.Text, entryMappings = entryMappings)

        val result = useCase(CLAIM_KEY, CLAIM_VALUE, ocaClaimData).assertOk()

        assertClaim(result.first)
        assertEquals(listOf(fallbackClaimDisplay), result.second)
    }

    @Test
    fun `Generating Oca claim displays with order returns claim with order`() = runTest {
        val ocaClaimData = createOcaClaimData(AttributeType.Text, order = 1)

        val result = useCase(CLAIM_KEY, CLAIM_VALUE, ocaClaimData).assertOk()

        assertClaim(result.first, order = 1)
    }

    @ParameterizedTest
    @EnumSource(value = ImageType::class)
    fun `Generating Oca claim displays for data URL returns ImageType claim`(imageType: ImageType) = runTest {
        val ocaClaimData = createOcaClaimData(AttributeType.Text, standard = Standard.DataUrl)

        val result = useCase(CLAIM_KEY, "data:${imageType.mimeType};base64,$CLAIM_VALUE", ocaClaimData).assertOk()

        assertClaim(result.first, value = CLAIM_VALUE, valueType = imageType.mimeType)
    }

    @Test
    fun `Generating Oca claim displays for null data URL returns string claim`() = runTest {
        val ocaClaimData = createOcaClaimData(AttributeType.Text, standard = Standard.DataUrl)

        val result = useCase(CLAIM_KEY, null, ocaClaimData).assertOk()

        assertClaim(result.first, value = null, valueType = ValueType.STRING.value)
    }

    @ParameterizedTest
    @EnumSource(value = ImageType::class)
    fun `Generating Oca claim displays for binary Image returns ImageType claim`(imageType: ImageType) = runTest {
        val ocaClaimData =
            createOcaClaimData(AttributeType.Binary, characterEncoding = CharacterEncoding.Base64, format = imageType.mimeType)

        val result = useCase(CLAIM_KEY, CLAIM_VALUE, ocaClaimData).assertOk()

        assertClaim(result.first, value = CLAIM_VALUE, valueType = imageType.mimeType)
    }

    @ParameterizedTest
    @EnumSource(value = ImageType::class)
    fun `Generating Oca claim displays for binary null value returns ImageType claim`(imageType: ImageType) = runTest {
        val ocaClaimData =
            createOcaClaimData(AttributeType.Binary, characterEncoding = CharacterEncoding.Base64, format = imageType.mimeType)

        val result = useCase(CLAIM_KEY, null, ocaClaimData).assertOk()

        assertClaim(result.first, value = null, valueType = imageType.mimeType)
    }

    @Test
    fun `Generating Oca claim displays for unknown data URL format returns string claim`() = runTest {
        val ocaClaimData = createOcaClaimData(AttributeType.Text, standard = Standard.DataUrl)
        val url = "data:unknown/unknown;base64,$CLAIM_VALUE"

        val result = useCase(CLAIM_KEY, url, ocaClaimData).assertOk()

        assertClaim(result.first, value = url, valueType = ValueType.STRING.value)
    }

    @Test
    fun `Generating Oca claim displays for unknown binary format returns string claim`() = runTest {
        val ocaClaimData =
            createOcaClaimData(AttributeType.Binary, characterEncoding = CharacterEncoding.Base64, format = "unknown/unknown")

        val result = useCase(CLAIM_KEY, CLAIM_VALUE, ocaClaimData).assertOk()

        assertClaim(result.first, valueType = ValueType.STRING.value)
    }

    @ParameterizedTest
    @EnumSource(value = ImageType::class)
    fun `Generating Oca claim displays for binary image without encoding returns string claim`(imageType: ImageType) = runTest {
        val ocaClaimData = createOcaClaimData(AttributeType.Text, characterEncoding = null, format = imageType.mimeType)

        val result = useCase(CLAIM_KEY, CLAIM_VALUE, ocaClaimData).assertOk()

        assertClaim(result.first, valueType = ValueType.STRING.value)
    }

    @ParameterizedTest
    @MethodSource("generateImageMimeType")
    fun `Generating Oca claim displays with image mimeType dataUrl trigger ValidateImage`(mimeType: String) {
        val ocaClaimData = createOcaClaimData(AttributeType.Text, standard = Standard.DataUrl)

        useCase(CLAIM_KEY, "data:$mimeType;base64,$CLAIM_VALUE", ocaClaimData).assertOk()

        coVerifyOrder {
            mockValidateImage(
                mimeType = mimeType,
                image = CLAIM_VALUE,
            )
        }
    }

    @Test
    fun `Generating Oca claim displays with non-image mimeType dataUrl does not trigger ValidateImage`() {
        val ocaClaimData = createOcaClaimData(AttributeType.Text, standard = Standard.DataUrl)
        val url = "data:text/plain;base64,$CLAIM_VALUE"

        useCase(CLAIM_KEY, url, ocaClaimData).assertOk()

        coVerify(exactly = 0) {
            mockValidateImage(
                mimeType = any(),
                image = any(),
            )
        }
    }

    @ParameterizedTest
    @MethodSource("generateImageMimeType")
    fun `Generating Oca claim displays for binary with Image mimeType trigger ValidateImage`(mimeType: String) = runTest {
        val ocaClaimData =
            createOcaClaimData(AttributeType.Binary, characterEncoding = CharacterEncoding.Base64, format = mimeType)

        useCase(CLAIM_KEY, CLAIM_VALUE, ocaClaimData).assertOk()

        coVerifyOrder {
            mockValidateImage(
                mimeType = mimeType,
                image = CLAIM_VALUE,
            )
        }
    }

    @Test
    fun `Generating Oca claim displays from a binary image map a ValidateImage error`() {
        every { mockValidateImage(any(), any()) } returns Err(ImageValidationError.UnsupportedImageFormat)

        val ocaClaimDataBinary =
            createOcaClaimData(AttributeType.Binary, characterEncoding = CharacterEncoding.Base64, format = ImageType.PNG.mimeType)

        useCase(CLAIM_KEY, CLAIM_VALUE, ocaClaimDataBinary).assertErrorType(OcaError.UnsupportedImageFormat::class)
    }

    @Test
    fun `Generating Oca claim displays from a dataUrl image map a ValidateImage error`() {
        every { mockValidateImage(any(), any()) } returns Err(ImageValidationError.UnsupportedImageFormat)

        val ocaClaimDataUrl = createOcaClaimData(AttributeType.Text, standard = Standard.DataUrl)

        useCase(
            CLAIM_KEY,
            "data:${ImageType.PNG.mimeType};base64,$CLAIM_VALUE",
            ocaClaimDataUrl
        ).assertErrorType(OcaError.UnsupportedImageFormat::class)
    }

    @ParameterizedTest
    @MethodSource("generateUnixTimeTestData")
    fun `Generating Oca claim displays for unix time returns date time claim`(
        dateTime: String,
        expectedValue: String,
        expectedValueDisplayInfo: String?
    ) = runTest {
        val ocaClaimData = createOcaClaimData(AttributeType.DateTime, standard = Standard.UnixTime)

        val result = useCase(CLAIM_KEY, dateTime, ocaClaimData).assertOk()

        assertClaim(
            result.first,
            value = expectedValue,
            valueType = ValueType.DATETIME.value,
            valueDisplayInfo = expectedValueDisplayInfo,
        )
    }

    @Test
    fun `Generating Oca claim displays for unix time without standard returns unparsed date time claim`() = runTest {
        val ocaClaimData = createOcaClaimData(AttributeType.DateTime, standard = null)

        val result = useCase(CLAIM_KEY, "1749167999", ocaClaimData).assertOk()

        assertClaim(result.first, value = "1749167999", valueType = ValueType.DATETIME.value)
    }

    @ParameterizedTest(name = "{index} should be {2}")
    @MethodSource("generateIso8601TestData")
    fun `Generating Oca claim displays for ISO time returns date time claim`(
        dateTime: String,
        expectedValue: String,
        expectedValueDisplayInfo: String?
    ) = runTest {
        val ocaClaimData = createOcaClaimData(AttributeType.DateTime, standard = Standard.Iso8601)

        val result = useCase(CLAIM_KEY, dateTime, ocaClaimData).assertOk()

        assertClaim(
            result.first,
            value = expectedValue,
            valueType = ValueType.DATETIME.value,
            valueDisplayInfo = expectedValueDisplayInfo,
        )
    }

    @Test
    fun `Generating Oca claim displays for ISO time without standard returns date time claim`() = runTest {
        val ocaClaimData = createOcaClaimData(AttributeType.DateTime, standard = null)

        val result = useCase(CLAIM_KEY, "2025-06-05T23:59:59+00:00", ocaClaimData).assertOk()

        assertClaim(
            result.first,
            value = "2025-06-05T23:59:59Z",
            valueType = ValueType.DATETIME.value,
            valueDisplayInfo = DateTimePattern.DATE_TIME_TIMEZONE_SECONDS.name,
        )
    }

    @Test
    fun `Generating Oca claim displays for null time returns date time claim`() = runTest {
        val ocaClaimData = createOcaClaimData(AttributeType.DateTime, standard = Standard.Iso8601)

        val result = useCase(CLAIM_KEY, null, ocaClaimData).assertOk()

        assertClaim(result.first, value = null, valueType = ValueType.DATETIME.value)
    }

    @Test
    fun `Generating Oca claim displays for array attribute returns string claim`() = runTest {
        val ocaClaimData = createOcaClaimData(AttributeType.Array(AttributeType.Text))

        val result = useCase(CLAIM_KEY, CLAIM_VALUE, ocaClaimData).assertOk()

        assertClaim(result.first, valueType = ValueType.STRING.value)
    }

    @Test
    fun `Generating Oca claim displays for reference attribute returns string claim`() = runTest {
        val ocaClaimData = createOcaClaimData(AttributeType.Reference("digest"))

        val result = useCase(CLAIM_KEY, CLAIM_VALUE, ocaClaimData).assertOk()

        assertClaim(result.first, valueType = ValueType.STRING.value)
    }

    @Test
    fun `Generating Oca claim displays for null value returns string claim`() = runTest {
        val ocaClaimData = createOcaClaimData(AttributeType.Text)

        val result = useCase(CLAIM_KEY, null, ocaClaimData).assertOk()

        assertClaim(result.first, value = null, valueType = ValueType.STRING.value)
    }

    private fun createLabels(locales: List<Locale>): Map<Locale, String> = locales.associateWith { "${LABEL}_$it" }

    private fun createEntryMappings(locales: List<Locale>, entryCode: EntryCode = ENTRY_CODE) =
        locales.associateWith { mapOf(entryCode to "${ENTRY_CODE}_$it") }

    private fun assertClaim(
        claim: CredentialClaim,
        value: String? = CLAIM_VALUE,
        valueType: String? = ValueType.STRING.value,
        valueDisplayInfo: String? = null,
        order: Int = -1
    ) {
        assertEquals(CLAIM_KEY, claim.path)
        assertEquals(value, claim.value)
        assertEquals(valueType, claim.valueType)
        assertEquals(valueDisplayInfo, claim.valueDisplayInfo)
        assertEquals(order, claim.order)
    }

    private fun createOcaClaimData(
        attributeType: AttributeType,
        characterEncoding: CharacterEncoding? = null,
        entryMappings: Map<Locale, Map<EntryCode, String>> = emptyMap(),
        format: String? = null,
        labels: Map<Locale, String> = emptyMap(),
        order: Int? = null,
        standard: Standard? = null,
    ) = OcaClaimData(
        attributeType,
        "captureBaseDigest",
        name = "name",
        characterEncoding = characterEncoding,
        entryMappings = entryMappings,
        format = format,
        labels = labels,
        order = order,
        standard = standard,
        isSensitive = false
    )

    private companion object {
        const val CLAIM_KEY = "claim_key"
        const val CLAIM_VALUE = "claim_value"
        const val LABEL = "label"
        const val ENTRY_CODE = "entryCode"

        val fallbackClaimDisplay = AnyClaimDisplay(name = CLAIM_KEY, locale = DisplayLanguage.FALLBACK)

        @JvmStatic
        fun generateImageMimeType(): Stream<Arguments> = Stream.of(
            Arguments.of(
                "image/tiff",
                "image/svg+xml",
                "image/webp",
                "image/jpeg",
                "image/png",
            )
        )

        @JvmStatic
        fun generateUnixTimeTestData(): Stream<Arguments> = Stream.of(
            // Unix time input string, expected output value, expected output pattern
            Arguments.of(
                "1749132605",
                "2025-06-05T14:10:05Z",
                DateTimePattern.DATE_TIME_TIMEZONE.name
            ),
            Arguments.of(
                "0",
                "1970-01-01T00:00Z",
                DateTimePattern.DATE_TIME_TIMEZONE.name
            ),
            Arguments.of(
                "invalid unix time",
                "invalid unix time",
                null
            ),
        )

        @JvmStatic
        fun generateIso8601TestData(): Stream<Arguments> = Stream.of(
            // ISO8601 input string, expected output value, expected output pattern
            Arguments.of(
                "2023-10-05T14:30:00.12Z",
                "2023-10-05T14:30:00.120Z",
                DateTimePattern.DATE_TIME_TIMEZONE_SECONDS_FRACTION.name
            ),
            Arguments.of(
                "2023-10-05T14:30:00.12-05:00",
                "2023-10-05T14:30:00.120-05:00",
                DateTimePattern.DATE_TIME_TIMEZONE_SECONDS_FRACTION.name
            ),
            Arguments.of(
                "2023-10-05T21:22:23.12+05:00",
                "2023-10-05T21:22:23.120+05:00",
                DateTimePattern.DATE_TIME_TIMEZONE_SECONDS_FRACTION.name,
            ),
            Arguments.of(
                "2023-10-05T14:30:00Z",
                "2023-10-05T14:30Z",
                DateTimePattern.DATE_TIME_TIMEZONE_SECONDS.name
            ),
            Arguments.of(
                "2023-10-05T14:30:00-05:00",
                "2023-10-05T14:30-05:00",
                DateTimePattern.DATE_TIME_TIMEZONE_SECONDS.name
            ),
            Arguments.of(
                "2022-12-31T23:59:59+00:00",
                "2022-12-31T23:59:59Z",
                DateTimePattern.DATE_TIME_TIMEZONE_SECONDS.name
            ),
            Arguments.of(
                "2023-10-05T21:22:23+05:00",
                "2023-10-05T21:22:23+05:00",
                DateTimePattern.DATE_TIME_TIMEZONE_SECONDS.name,
            ),
            Arguments.of(
                "2023-10-05T21:22:23Z",
                "2023-10-05T21:22:23Z",
                DateTimePattern.DATE_TIME_TIMEZONE_SECONDS.name,
            ),
            Arguments.of(
                "2023-10-05t21:22:23Z",
                "2023-10-05T21:22:23Z",
                DateTimePattern.DATE_TIME_TIMEZONE_SECONDS.name,
            ),
            Arguments.of(
                "2007-04-05T14:30Z",
                "2007-04-05T14:30Z",
                DateTimePattern.DATE_TIME_TIMEZONE.name
            ),
            Arguments.of(
                "2007-04-05T14:30-02:30",
                "2007-04-05T14:30-02:30",
                DateTimePattern.DATE_TIME_TIMEZONE.name
            ),
            Arguments.of(
                "2022-12-31T23:59:01.123",
                "2022-12-31T23:59:01.123Z",
                DateTimePattern.DATE_TIME_SECONDS_FRACTION.name
            ),
            Arguments.of(
                "2022-12-31T23:59:23.12",
                "2022-12-31T23:59:23.120Z",
                DateTimePattern.DATE_TIME_SECONDS_FRACTION.name
            ),
            Arguments.of(
                "2022-12-31T23:59:23",
                "2022-12-31T23:59:23Z",
                DateTimePattern.DATE_TIME_SECONDS.name
            ),
            Arguments.of(
                "2022-12-31T23:59:00",
                "2022-12-31T23:59Z",
                DateTimePattern.DATE_TIME_SECONDS.name
            ),
            Arguments.of(
                "2022-12-31T23:59",
                "2022-12-31T23:59Z",
                DateTimePattern.DATE_TIME.name
            ),
            Arguments.of(
                "2023-10-05+05:00",
                "2023-10-05T00:00+05:00",
                DateTimePattern.DATE_TIMEZONE.name,
            ),
            Arguments.of(
                "2023-10-05Z",
                "2023-10-05T00:00Z",
                DateTimePattern.DATE_TIMEZONE.name,
            ),
            Arguments.of(
                "2023-10-05",
                "2023-10-05T00:00Z",
                DateTimePattern.DATE.name
            ),

            Arguments.of(
                "10:10:10.12Z",
                "0000-01-01T10:10:10.120Z",
                DateTimePattern.TIME_TIMEZONE_SECONDS_FRACTION.name
            ),
            Arguments.of(
                "10:10:10.123+05:30",
                "0000-01-01T10:10:10.123+05:30",
                DateTimePattern.TIME_TIMEZONE_SECONDS_FRACTION.name
            ),
            Arguments.of(
                "10:10:10Z",
                "0000-01-01T10:10:10Z",
                DateTimePattern.TIME_TIMEZONE_SECONDS.name
            ),
            Arguments.of(
                "10:10:10-03:00",
                "0000-01-01T10:10:10-03:00",
                DateTimePattern.TIME_TIMEZONE_SECONDS.name
            ),
            Arguments.of(
                "10:10Z",
                "0000-01-01T10:10Z",
                DateTimePattern.TIME_TIMEZONE.name
            ),
            Arguments.of(
                "10:10+05:00",
                "0000-01-01T10:10+05:00",
                DateTimePattern.TIME_TIMEZONE.name
            ),
            Arguments.of(
                "10:10:10.12",
                "0000-01-01T10:10:10.120Z",
                DateTimePattern.TIME_SECONDS_FRACTION.name
            ),
            Arguments.of(
                "10:10:10.123456789",
                "0000-01-01T10:10:10.123456789Z",
                DateTimePattern.TIME_SECONDS_FRACTION.name
            ),
            Arguments.of(
                "10:10:10",
                "0000-01-01T10:10:10Z",
                DateTimePattern.TIME_SECONDS.name
            ),
            Arguments.of(
                "10:10:00",
                "0000-01-01T10:10Z",
                DateTimePattern.TIME_SECONDS.name
            ),
            Arguments.of(
                "10:10",
                "0000-01-01T10:10Z",
                DateTimePattern.TIME.name
            ),
            Arguments.of(
                "2023-10",
                "2023-10-01T00:00Z",
                DateTimePattern.YEAR_MONTH.name
            ),
            Arguments.of(
                "2023-10",
                "2023-10-01T00:00Z",
                DateTimePattern.YEAR_MONTH.name
            ),
            Arguments.of(
                "2023",
                "2023-01-01T00:00Z",
                DateTimePattern.YEAR.name
            ),
            Arguments.of(
                "-2023-10",
                "-2023-10-01T00:00Z",
                DateTimePattern.YEAR_MONTH.name
            ),
            // Too many decimal fraction
            Arguments.of(
                "10:10:10.1234567890",
                "10:10:10.1234567890",
                null
            ),
            Arguments.of(
                "invalid iso8601",
                "invalid iso8601",
                null
            )
        )
    }
}
