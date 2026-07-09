package ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointerComponent
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.toPointerString
import ch.admin.foitt.wallet.platform.oca.domain.model.AttributeType
import ch.admin.foitt.wallet.platform.oca.domain.model.CaptureBase1x0
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.BrandingOverlay1x1
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.DataSourceOverlay2x0
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.LabelOverlay1x1
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.Overlay
import ch.admin.foitt.wallet.platform.oca.domain.usecase.TransformOcaOverlays
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class TransformOcaOverlaysImplTest {

    private lateinit var transformOcaOverlays: TransformOcaOverlays

    @BeforeEach
    fun setup() {
        transformOcaOverlays = TransformOcaOverlaysImpl()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @TestFactory
    fun `Oca bundler correctly resolves branding overlay template`(): List<DynamicTest> {
        val input = listOf(
            dataSourceOverlayV2,
        )

        return input.map { dataSourceOverlay ->
            DynamicTest.dynamicTest("Oca bundler correctly resolves branding overlay with datasource overlay") {
                runTest {
                    val overlays = listOf(dataSourceOverlay, brandingOverlaySingleTemplate)

                    val result = transformOcaOverlays(overlays, captureBases)

                    assertEquals(2, result.size)

                    assertEquals("Primary: {{$ATTRIBUTE_CLAIMS_PATH_POINTER_STRING}}", (result[1] as BrandingOverlay1x1).primaryField)
                }
            }
        }
    }

    @TestFactory
    fun `Oca bundler correctly resolves label overlay template`(): List<DynamicTest> {
        val input = listOf(
            dataSourceOverlayV2,
        )

        return input.map { dataSourceOverlay ->
            DynamicTest.dynamicTest("Oca bundler correctly resolves label overlay with datasource overlay") {
                runTest {
                    val labelOverlay = createLabelOverlay("Label: {{$ATTRIBUTE_KEY}}")
                    val overlays = listOf(dataSourceOverlay, labelOverlay)

                    val result = transformOcaOverlays(overlays, captureBases)

                    assertEquals(2, result.size)

                    assertEquals(
                        "Label: {{$ATTRIBUTE_CLAIMS_PATH_POINTER_STRING}}",
                        (result[1] as LabelOverlay1x1).attributeLabels.getValue(ATTRIBUTE_KEY)
                    )
                }
            }
        }
    }

    @Test
    fun `Oca bundler correctly resolves branding overlay multi template`() = runTest {
        val overlays = listOf(dataSourceOverlayV2, brandingOverlayMultiTemplate)

        val result = transformOcaOverlays(overlays, captureBases)

        assertEquals(2, result.size)

        assertEquals(
            "Primary: {{$ATTRIBUTE_CLAIMS_PATH_POINTER_STRING}}, {{$ATTRIBUTE2_CLAIMS_PATH_POINTER_STRING}}",
            (result[1] as BrandingOverlay1x1).primaryField
        )
    }

    @Test
    fun `Oca bundle where template attribute is not in data source is replaced by empty string`() = runTest {
        val overlays = listOf(dataSourceOverlayV2, brandingOverlayTemplateAttributeOther)

        val result = transformOcaOverlays(overlays, captureBases)

        assertEquals(2, result.size)

        assertEquals("Primary: ", (result[1] as BrandingOverlay1x1).primaryField)
    }

    @Test
    fun `Oca bundle where no primary field is provided returns null`() = runTest {
        val overlays = listOf(dataSourceOverlayV2, brandingOverlayNoPrimaryField)

        val result = transformOcaOverlays(overlays, captureBases)

        assertEquals(2, result.size)

        assertEquals(null, (result[1] as BrandingOverlay1x1).primaryField)
    }

    @Test
    fun `Oca bundle where data source and branding overlay have different digests replaces by empty string`() = runTest {
        val overlays = listOf(dataSourceOverlayOtherDigest, brandingOverlaySingleTemplate)

        val result = transformOcaOverlays(overlays, captureBases)

        assertEquals(2, result.size)

        assertEquals("Primary: ", (result[1] as BrandingOverlay1x1).primaryField)
    }

    @Test
    fun `Oca bundle with primary and secondary field is correctly resolved`() = runTest {
        val overlays = listOf(dataSourceOverlayV2, brandingOverlayPrimaryAndSecondaryField)

        val result = transformOcaOverlays(overlays, captureBases)

        assertEquals(2, result.size)

        val brandingOverlay = result[1] as BrandingOverlay1x1
        assertEquals("Primary: {{$ATTRIBUTE_CLAIMS_PATH_POINTER_STRING}}", brandingOverlay.primaryField)
        assertEquals("Secondary: {{$ATTRIBUTE2_CLAIMS_PATH_POINTER_STRING}}", brandingOverlay.secondaryField)
    }

    @Test
    fun `Oca bundle with primary and secondary field that contain the same attribute is correctly resolved`() = runTest {
        val overlays = listOf(dataSourceOverlayV2, brandingOverlayPrimaryAndSecondaryFieldSameAttribute)

        val result = transformOcaOverlays(overlays, captureBases)

        assertEquals(2, result.size)

        val brandingOverlay = result[1] as BrandingOverlay1x1
        assertEquals("Primary: {{$ATTRIBUTE_CLAIMS_PATH_POINTER_STRING}}", brandingOverlay.primaryField)
        assertEquals("Secondary: {{$ATTRIBUTE_CLAIMS_PATH_POINTER_STRING}}", brandingOverlay.secondaryField)
    }

    @Test
    fun `Oca bundle with only secondary field is correctly resolved`() = runTest {
        val overlays = listOf(dataSourceOverlayV2, brandingOverlayOnlySecondaryField)

        val result = transformOcaOverlays(overlays, captureBases)

        assertEquals(2, result.size)

        val brandingOverlay = result[1] as BrandingOverlay1x1
        assertEquals(null, brandingOverlay.primaryField)
        assertEquals("Secondary: {{$ATTRIBUTE_CLAIMS_PATH_POINTER_STRING}}", brandingOverlay.secondaryField)
    }

    @Test
    fun `Oca bundle where primary field references attribute from other capture base is correctly resolved`() = runTest {
        val overlays = listOf(dataSourceOverlayCaptureBase2, brandingOverlayWithReference)

        val result = transformOcaOverlays(overlays, captureBases)

        assertEquals(2, result.size)

        val brandingOverlay = result[1] as BrandingOverlay1x1
        assertEquals("Primary: {{$ATTRIBUTE_CLAIMS_PATH_POINTER_STRING}}", brandingOverlay.primaryField)
        assertEquals(null, brandingOverlay.secondaryField)
    }

    @Test
    fun `Oca bundle where primary field references multiple attributes from other capture bases is correctly resolved`() = runTest {
        val overlays =
            listOf(dataSourceOverlayCaptureBase2, dataSourceOverlayCaptureBase3, brandingOverlayWithMultiReference)

        val result = transformOcaOverlays(overlays, captureBases)

        assertEquals(3, result.size)

        val brandingOverlay = result[2] as BrandingOverlay1x1
        assertEquals(
            "Primary: {{$ATTRIBUTE_CLAIMS_PATH_POINTER_STRING}} and {{$ATTRIBUTE2_CLAIMS_PATH_POINTER_STRING}}",
            brandingOverlay.primaryField
        )
        assertEquals(null, brandingOverlay.secondaryField)
    }

    @TestFactory
    fun `Valid simple references are replaced correctly`(): List<DynamicTest> {
        return validSimpleFields.map { (input, output) ->
            DynamicTest.dynamicTest("Input $input should be replaced by $output") {
                runTest {
                    val overlay = createBrandingOverlay(primaryField = "Primary: $input")
                    val overlays = listOf(dataSourceOverlayV2, overlay)
                    val result = transformOcaOverlays(overlays, captureBases)
                    assertEquals(2, result.size)

                    val brandingOverlay = result[1] as BrandingOverlay1x1
                    assertEquals("Primary: {{$output}}", brandingOverlay.primaryField)
                }
            }
        }
    }

    @TestFactory
    fun `Valid array references are replaced correctly`(): List<DynamicTest> {
        return validArrayReferences.map { (input, output) ->
            DynamicTest.dynamicTest("Input $input should be replaced by $output") {
                runTest {
                    val overlay = createBrandingOverlay(primaryField = "Primary: $input")
                    val overlays = listOf(dataSourceOverlayV2, overlay)
                    val result = transformOcaOverlays(overlays, captureBasesNested)
                    assertEquals(2, result.size)

                    val brandingOverlay = result[1] as BrandingOverlay1x1
                    assertEquals("Primary: {{$output}}", brandingOverlay.primaryField)
                }
            }
        }
    }

    @TestFactory
    fun `Invalid references are replaced by empty string`(): List<DynamicTest> {
        return invalidFields.map { input ->
            DynamicTest.dynamicTest("Input $input should be replaced by empty string") {
                runTest {
                    val overlay = createBrandingOverlay(primaryField = "Primary: $input")
                    val overlays = listOf(dataSourceOverlayV2, overlay)
                    val result = transformOcaOverlays(overlays, captureBases)
                    assertEquals(2, result.size)

                    val brandingOverlay = result[1] as BrandingOverlay1x1
                    assertEquals("Primary: ", brandingOverlay.primaryField)
                }
            }
        }
    }

    private val captureBases = listOf(
        CaptureBase1x0(
            digest = "digest",
            attributes = mapOf(
                ATTRIBUTE_KEY to AttributeType.Text,
                ATTRIBUTE2_KEY to AttributeType.Text,
            )
        ),
    )

    private val captureBasesNested = listOf(
        CaptureBase1x0(
            digest = "digest",
            attributes = mapOf(
                ATTRIBUTE_KEY to AttributeType.Array(AttributeType.Reference("digest2"))
            )
        ),
        CaptureBase1x0(
            digest = "digest2",
            attributes = mapOf(
                ATTRIBUTE2_KEY to AttributeType.Text
            )
        )
    )

    private val dataSourceOverlayV2 = createDataSourceOverlayV2x0()
    private val dataSourceOverlayOtherDigest = createDataSourceOverlayV2x0("otherDigest")
    private val dataSourceOverlayCaptureBase2 = createDataSourceOverlayV2x0("digest2")
    private val dataSourceOverlayCaptureBase3 = createDataSourceOverlayV2x0("digest3")

    private val brandingOverlaySingleTemplate = createBrandingOverlay("Primary: {{$ATTRIBUTE_KEY}}")
    private val brandingOverlayMultiTemplate = createBrandingOverlay("Primary: {{$ATTRIBUTE_KEY}}, {{$ATTRIBUTE2_KEY}}")
    private val brandingOverlayTemplateAttributeOther = createBrandingOverlay("Primary: {{attributeKeyOther}}")
    private val brandingOverlayNoPrimaryField = createBrandingOverlay(null)
    private val brandingOverlayPrimaryAndSecondaryField =
        createBrandingOverlay("Primary: {{$ATTRIBUTE_KEY}}", "Secondary: {{$ATTRIBUTE2_KEY}}")
    private val brandingOverlayPrimaryAndSecondaryFieldSameAttribute =
        createBrandingOverlay("Primary: {{$ATTRIBUTE_KEY}}", "Secondary: {{$ATTRIBUTE_KEY}}")
    private val brandingOverlayOnlySecondaryField = createBrandingOverlay(null, "Secondary: {{$ATTRIBUTE_KEY}}")
    private val brandingOverlayWithReference = createBrandingOverlay("Primary: {{refs:digest2:$ATTRIBUTE_KEY}}")
    private val brandingOverlayWithMultiReference =
        createBrandingOverlay("Primary: {{refs:digest2:$ATTRIBUTE_KEY}} and {{refs:digest3:$ATTRIBUTE2_KEY}}")

    private val validSimpleFields = listOf(
        "{{refs:digest:attribute}}" to ATTRIBUTE_CLAIMS_PATH_POINTER_STRING,
        "{{refs:digest:attribute.join(\" \")}}" to "$ATTRIBUTE_CLAIMS_PATH_POINTER_STRING.join(\" \")",
        "{{refs:digest:attribute.join(' ')}}" to "$ATTRIBUTE_CLAIMS_PATH_POINTER_STRING.join(' ')",
    )

    private val validArrayReferences = listOf(
        "{{attribute[0]}}" to ATTRIBUTE_0_CLAIMS_PATH_POINTER_STRING,
        "{{attribute[0].join(' ')}}" to "$ATTRIBUTE_0_CLAIMS_PATH_POINTER_STRING.join(' ')",
        "{{attribute[0].join(\" \")}}" to "$ATTRIBUTE_0_CLAIMS_PATH_POINTER_STRING.join(\" \")",
        "{{attribute[null]}}" to ATTRIBUTE_ARRAY_CLAIMS_PATH_POINTER_STRING,
        "{{attribute[null].join(' ')}}" to "$ATTRIBUTE_ARRAY_CLAIMS_PATH_POINTER_STRING.join(' ')",
        "{{attribute[null].join(\" \")}}" to "$ATTRIBUTE_ARRAY_CLAIMS_PATH_POINTER_STRING.join(\" \")",
        "{{refs:digest:attribute[0]}}" to ATTRIBUTE_0_CLAIMS_PATH_POINTER_STRING,
        "{{refs:digest:attribute[0].join(' ')}}" to "$ATTRIBUTE_0_CLAIMS_PATH_POINTER_STRING.join(' ')",
        "{{refs:digest:attribute[0].join(\" \")}}" to "$ATTRIBUTE_0_CLAIMS_PATH_POINTER_STRING.join(\" \")",
        "{{refs:digest:attribute[null]}}" to ATTRIBUTE_ARRAY_CLAIMS_PATH_POINTER_STRING,
        "{{refs:digest:attribute[null].join(' ')}}" to "$ATTRIBUTE_ARRAY_CLAIMS_PATH_POINTER_STRING.join(' ')",
        "{{refs:digest:attribute[null].join(\" \")}}" to "$ATTRIBUTE_ARRAY_CLAIMS_PATH_POINTER_STRING.join(\" \")",
    )

    private val invalidFields = listOf(
        "{{}}",
        "{{ref}}",
        "{{:attribute}}",
        "{{invalidRef:digest:attribute}}",
        "{{refs::digest:attribute}}",
        "{{refs::digest::attribute}}",
        "{{refs::digest:attribute[null].join(' ')}}",
        "{{refs:digest::attribute[null].join(' ')}}",
        "{{refs:digest:attribute[null]..join(' ')}}",
        "{{refs:digest:attribute[null].join( )}}",
        "{{refs:digest:attribute[null].join(' )}}",
        "{{refs:digest:attribute[null].join( ')}}",
        "{{refs:digest:attribute[null].join(\" )}}",
        "{{refs:digest:attribute[null].join( \")}}",
        "{{refs:digest:attribute[null].join(' \")}}",
        "{{refs:digest:attribute[null].join(\" ')}}",
        "{{refs:digest:attribute[null].join('moreThan10Chars')}}",
        "{{Test: {{(attribute1)}}}}"
    )

    companion object {
        private const val ATTRIBUTE_KEY = "attribute"
        private const val ATTRIBUTE2_KEY = "attribute2"
        private val ATTRIBUTE_CLAIMS_PATH_POINTER = listOf(ClaimsPathPointerComponent.String("attribute"))
        private val ATTRIBUTE_CLAIMS_PATH_POINTER_STRING = ATTRIBUTE_CLAIMS_PATH_POINTER.toPointerString()
        private val ATTRIBUTE_0_CLAIMS_PATH_POINTER = listOf(
            ClaimsPathPointerComponent.String("attribute"),
            ClaimsPathPointerComponent.Index(0),
        )
        private val ATTRIBUTE_0_CLAIMS_PATH_POINTER_STRING = ATTRIBUTE_0_CLAIMS_PATH_POINTER.toPointerString()
        private val ATTRIBUTE_ARRAY_CLAIMS_PATH_POINTER = listOf(
            ClaimsPathPointerComponent.String("attribute"),
            ClaimsPathPointerComponent.Null,
        )
        private val ATTRIBUTE_ARRAY_CLAIMS_PATH_POINTER_STRING = ATTRIBUTE_ARRAY_CLAIMS_PATH_POINTER.toPointerString()
        private val ATTRIBUTE2_CLAIMS_PATH_POINTER = listOf(ClaimsPathPointerComponent.String("attribute2"))
        private val ATTRIBUTE2_CLAIMS_PATH_POINTER_STRING = ATTRIBUTE2_CLAIMS_PATH_POINTER.toPointerString()

        private fun createDataSourceOverlayV2x0(captureBase: String = "digest"): Overlay = DataSourceOverlay2x0(
            captureBaseDigest = captureBase,
            format = "vc+sd-jwt",
            attributeSources = mapOf(
                ATTRIBUTE_KEY to ATTRIBUTE_CLAIMS_PATH_POINTER,
                ATTRIBUTE2_KEY to ATTRIBUTE2_CLAIMS_PATH_POINTER,
            ),
        )

        private fun createBrandingOverlay(primaryField: String?, secondaryField: String? = null): Overlay = BrandingOverlay1x1(
            captureBaseDigest = "digest",
            language = "en",
            primaryField = primaryField,
            secondaryField = secondaryField,
        )

        private fun createLabelOverlay(label: String): Overlay = LabelOverlay1x1(
            captureBaseDigest = "digest",
            language = "en",
            attributeLabels = mapOf(
                ATTRIBUTE_KEY to label
            ),
        )
    }
}
