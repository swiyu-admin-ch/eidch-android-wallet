package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointerComponent
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.toPointerString
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.Claim
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.OidClaimDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyClaimDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GenerateMetadataClaimDisplays
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage
import ch.admin.foitt.wallet.platform.imageValidation.domain.model.ImageValidationError
import ch.admin.foitt.wallet.platform.imageValidation.domain.usecase.ValidateImage
import ch.admin.foitt.wallet.platform.ssi.domain.model.ImageType
import ch.admin.foitt.wallet.platform.ssi.domain.model.ImageType.Companion.base64MagicNumber
import ch.admin.foitt.wallet.platform.ssi.domain.model.ValueType
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class GenerateMetadataClaimDisplaysImplTest {

    @MockK
    private lateinit var mockValidateImage: ValidateImage

    private lateinit var useCase: GenerateMetadataClaimDisplays

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = GenerateMetadataClaimDisplaysImpl(
            validateImage = mockValidateImage,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    private fun setupDefaultMocks() {
        every { mockValidateImage(mimeType = any(), image = any()) } returns Ok(Unit)
    }

    @Test
    fun `Generating metadata claim displays returns expected claim displays`() = runTest {
        val result = useCase(
            claimsPathPointer = claimPathPointer,
            claimValueJson = claimValueJsonElement,
            metadataClaim = metadataClaim01,
            order = -1,
        ).assertOk()

        assertEquals(credentialClaim01, result.first)
        assertEquals(credentialClaim01Displays, result.second)
    }

    @Test
    fun `Generating metadata claim displays adds fallback language if empty`() = runTest {
        val result = useCase(
            claimsPathPointer = claimPathPointer,
            claimValueJson = claimValueJsonElement,
            metadataClaim = metadataClaimNoDisplay,
            order = -1
        ).assertOk()

        val expectedClaimDisplays = listOf(
            AnyClaimDisplay(locale = DisplayLanguage.FALLBACK, name = claimPathPointerString)
        )

        assertEquals(expectedClaimDisplays, result.second)
    }

    @TestFactory
    fun `Generating metadata displays correctly parses the value type of the claim`(): List<DynamicTest> {
        val input = listOf(
            Triple(
                JsonPrimitive(1),
                "1",
                ValueType.NUMERIC.value
            ),
            Triple(
                JsonPrimitive("true"),
                "true",
                ValueType.BOOLEAN.value
            ),
            Triple(
                JsonPrimitive("data:image/png;base64,${ImageType.PNG.base64MagicNumber}"),
                ImageType.PNG.base64MagicNumber,
                ImageType.PNG.mimeType
            ),
            Triple(
                JsonPrimitive("data:image/jpeg;base64,${ImageType.JPEG.base64MagicNumber}"),
                ImageType.JPEG.base64MagicNumber,
                ImageType.JPEG.mimeType
            ),
            Triple(
                JsonPrimitive(ImageType.PNG.base64MagicNumber),
                ImageType.PNG.base64MagicNumber,
                ImageType.PNG.mimeType
            ),
            Triple(
                JsonPrimitive(ImageType.JPEG.base64MagicNumber),
                ImageType.JPEG.base64MagicNumber,
                ImageType.JPEG.mimeType
            ),
            Triple(
                JsonPrimitive("2026-03-16T09:23:50"),
                "2026-03-16T09:23:50",
                ValueType.DATETIME.value
            ),
            Triple(
                JsonPrimitive("claim value text"),
                "claim value text",
                ValueType.STRING.value
            ),
            Triple(
                buildJsonArray { add(JsonPrimitive("arrayElement")) },
                "[\"arrayElement\"]",
                ValueType.STRING.value
            ),
            Triple(
                buildJsonObject { put("key", JsonPrimitive("object")) },
                "{\"key\":\"object\"}",
                ValueType.STRING.value
            ),
        )

        return input.map { (claimValue, expectedValue, expectedValueType) ->
            DynamicTest.dynamicTest("Value $claimValue should return $expectedValue and $expectedValueType") {
                runTest {
                    val result = useCase(
                        claimsPathPointer = claimPathPointer,
                        claimValueJson = claimValue,
                        metadataClaim = null,
                        order = -1,
                    ).assertOk()

                    val expectedClaim = CredentialClaim(
                        clusterId = -1,
                        path = claimPathPointerString,
                        value = expectedValue,
                        valueType = expectedValueType,
                        order = -1,
                    )

                    assertEquals(expectedClaim, result.first)
                }
            }
        }
    }

    @Test
    fun `Generating metadata claim displays maps errors from image validation`() = runTest {
        every { mockValidateImage(mimeType = any(), image = any()) } returns Err(ImageValidationError.UnsupportedImageFormat)

        useCase(
            claimsPathPointer = claimPathPointer,
            claimValueJson = claimValueDataUriJson,
            metadataClaim = null,
            order = -1,
        ).assertErrorType(CredentialError.UnsupportedImageFormat::class)
    }

    private companion object {
        val claimPathPointer = listOf(ClaimsPathPointerComponent.String("claim"))
        val claimPathPointerString = claimPathPointer.toPointerString()
        const val CLAIM_VALUE = "claim_value"
        val claimValueJsonElement = JsonPrimitive(CLAIM_VALUE)
        const val CLAIM_VALUE_DATA_URI = "data:image/png;base64,imageData"
        val claimValueDataUriJson = JsonPrimitive(CLAIM_VALUE_DATA_URI)
        val claimType = ValueType.STRING
        val claimTypeString = claimType.value
        const val LANGUAGE_EN = "en"
        const val CLAIM_VALUE_EN = "claim value en"

        val claim01Displays = listOf(
            OidClaimDisplay(locale = LANGUAGE_EN, name = CLAIM_VALUE_EN),
        )

        val metadataClaim01 = Claim(
            path = claimPathPointer,
            mandatory = true,
            display = claim01Displays,
        )

        val metadataClaimNoDisplay = Claim(
            path = claimPathPointer,
            mandatory = true,
            display = null,
        )

        val credentialClaim01 = CredentialClaim(
            clusterId = -1,
            path = claimPathPointerString,
            value = CLAIM_VALUE,
            valueType = claimTypeString,
        )

        val credentialClaim01Displays = listOf(
            AnyClaimDisplay(locale = LANGUAGE_EN, name = CLAIM_VALUE_EN),
            AnyClaimDisplay(locale = DisplayLanguage.FALLBACK, name = claimPathPointerString),
        )
    }
}
