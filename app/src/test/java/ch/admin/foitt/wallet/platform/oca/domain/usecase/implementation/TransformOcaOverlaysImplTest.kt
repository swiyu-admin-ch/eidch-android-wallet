package ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.BrandingOverlay1x1
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.Overlay
import ch.admin.foitt.wallet.platform.oca.domain.usecase.TransformOcaOverlays
import ch.admin.foitt.wallet.util.SafeJsonTestInstance
import com.github.michaelbull.result.annotation.UnsafeResultValueAccess
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

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
    fun `Oca bundler correctly resolves branding overlay single template`(): List<DynamicTest> {
        val input = mapOf(
            dataSourceOverlay to ATTRIBUTE_JSON_PATH,
            dataSourceOverlayV2 to ATTRIBUTE_CLAIMS_PATH_POINTER,
        )

        return input.map { (dataSourceOverlay, expectedString) ->
            DynamicTest.dynamicTest("Oca bundler correctly resolves branding overlay with datasource overlay") {
                runTest {
                    val overlays = listOf(dataSourceOverlay, brandingOverlaySingleTemplate)

                    val result = transformOcaOverlays(overlays)

                    assertEquals(2, result.size)

                    assertEquals("Primary: {{$expectedString}}", (result[1] as BrandingOverlay1x1).primaryField)
                }
            }
        }
    }

    @Test
    fun `Oca bundler correctly resolves branding overlay multi template`() = runTest {
        val overlays = listOf(dataSourceOverlay, brandingOverlayMultiTemplate)

        val result = transformOcaOverlays(overlays)

        assertEquals(2, result.size)

        assertEquals(
            "Primary: {{$ATTRIBUTE_JSON_PATH}}, {{$ATTRIBUTE2_JSON_PATH}}",
            (result[1] as BrandingOverlay1x1).primaryField
        )
    }

    @Test
    fun `Oca bundle where template attribute is not in data source is replaced by empty string`() = runTest {
        val overlays = listOf(dataSourceOverlay, brandingOverlayTemplateAttributeOther)

        val result = transformOcaOverlays(overlays)

        assertEquals(2, result.size)

        assertEquals("Primary: ", (result[1] as BrandingOverlay1x1).primaryField)
    }

    @Test
    fun `Oca bundle where no primary field is provided returns null`() = runTest {
        val overlays = listOf(dataSourceOverlay, brandingOverlayNoPrimaryField)

        val result = transformOcaOverlays(overlays)

        assertEquals(2, result.size)

        assertEquals(null, (result[1] as BrandingOverlay1x1).primaryField)
    }

    @Test
    fun `Oca bundle where data source and branding overlay have different digests replaces by empty string`() = runTest {
        val overlays = listOf(dataSourceOverlayOtherDigest, brandingOverlaySingleTemplate)

        val result = transformOcaOverlays(overlays)

        assertEquals(2, result.size)

        assertEquals("Primary: ", (result[1] as BrandingOverlay1x1).primaryField)
    }

    @Test
    fun `Oca bundle with primary and secondary field is correctly resolved`() = runTest {
        val overlays = listOf(dataSourceOverlay, brandingOverlayPrimaryAndSecondaryField)

        val result = transformOcaOverlays(overlays)

        assertEquals(2, result.size)

        val brandingOverlay = result[1] as BrandingOverlay1x1
        assertEquals("Primary: {{$ATTRIBUTE_JSON_PATH}}", brandingOverlay.primaryField)
        assertEquals("Secondary: {{$ATTRIBUTE2_JSON_PATH}}", brandingOverlay.secondaryField)
    }

    @Test
    fun `Oca bundle with primary and secondary field that contain the same attribute is correctly resolved`() = runTest {
        val overlays = listOf(dataSourceOverlay, brandingOverlayPrimaryAndSecondaryFieldSameAttribute)

        val result = transformOcaOverlays(overlays)

        assertEquals(2, result.size)

        val brandingOverlay = result[1] as BrandingOverlay1x1
        assertEquals("Primary: {{$ATTRIBUTE_JSON_PATH}}", brandingOverlay.primaryField)
        assertEquals("Secondary: {{$ATTRIBUTE_JSON_PATH}}", brandingOverlay.secondaryField)
    }

    @Test
    fun `Oca bundle with only secondary field is correctly resolved`() = runTest {
        val overlays = listOf(dataSourceOverlay, brandingOverlayOnlySecondaryField)

        val result = transformOcaOverlays(overlays)

        assertEquals(2, result.size)

        val brandingOverlay = result[1] as BrandingOverlay1x1
        assertEquals(null, brandingOverlay.primaryField)
        assertEquals("Secondary: {{$ATTRIBUTE_JSON_PATH}}", brandingOverlay.secondaryField)
    }

    @Test
    fun `Oca bundle where primary field references attribute from other capture base is correctly resolved`() = runTest {
        val overlays = listOf(dataSourceOverlayCaptureBase2, brandingOverlayWithReference)

        val result = transformOcaOverlays(overlays)

        assertEquals(2, result.size)

        val brandingOverlay = result[1] as BrandingOverlay1x1
        assertEquals("Primary: {{$ATTRIBUTE_JSON_PATH}}", brandingOverlay.primaryField)
        assertEquals(null, brandingOverlay.secondaryField)
    }

    @Test
    fun `Oca bundle where primary field references multiple attributes from other capture bases is correctly resolved`() = runTest {
        val overlays =
            listOf(dataSourceOverlayCaptureBase2, dataSourceOverlayCaptureBase3, brandingOverlayWithMultiReference)

        val result = transformOcaOverlays(overlays)

        assertEquals(3, result.size)

        val brandingOverlay = result[2] as BrandingOverlay1x1
        assertEquals("Primary: {{$ATTRIBUTE_JSON_PATH}} and {{$ATTRIBUTE2_JSON_PATH}}", brandingOverlay.primaryField)
        assertEquals(null, brandingOverlay.secondaryField)
    }

    @Test
    fun `Invalid References in branding overlay are replaced by empty string`() = runTest {
        val overlays = listOf(dataSourceOverlayCaptureBase2, brandingOverlayWithInvalidReference)

        val result = transformOcaOverlays(overlays)

        assertEquals(2, result.size)

        val brandingOverlay = result[1] as BrandingOverlay1x1
        assertEquals("Primary: ", brandingOverlay.primaryField)
        assertEquals(null, brandingOverlay.secondaryField)
    }

    @ParameterizedTest
    @MethodSource("generateInvalidAttributes")
    fun `Invalid references are replaced by empty string`(input: Overlay) = runTest {
        val overlays = listOf(dataSourceOverlayCaptureBase2, input)

        val result = transformOcaOverlays(overlays)

        assertEquals(2, result.size)

        val brandingOverlay = result[1] as BrandingOverlay1x1
        assertEquals("Primary: ", brandingOverlay.primaryField)
        assertEquals(null, brandingOverlay.secondaryField)
    }

    private val dataSourceOverlay = createDataSourceOverlay()
    private val dataSourceOverlayV2 = createDataSourceOverlayV2x0()
    private val dataSourceOverlayOtherDigest = createDataSourceOverlay("otherDigest")
    private val dataSourceOverlayCaptureBase2 = createDataSourceOverlay("digest2")
    private val dataSourceOverlayCaptureBase3 = createDataSourceOverlay("digest3")

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
    private val brandingOverlayWithInvalidReference =
        createBrandingOverlay("Primary: {{invalidRef:digest2:$ATTRIBUTE_KEY}}")

    @OptIn(UnsafeResultValueAccess::class)
    companion object {
        private val json = SafeJsonTestInstance.safeJson
        private const val ATTRIBUTE_KEY = "attributeKey"
        private const val ATTRIBUTE2_KEY = "attribute2Key"
        private const val ATTRIBUTE_JSON_PATH = "$.attributeJsonPath"
        private const val ATTRIBUTE2_JSON_PATH = "$.attribute2JsonPath"
        private const val ATTRIBUTE_CLAIMS_PATH_POINTER = "[\"attribute\"]"

        private fun createDataSourceOverlay(captureBase: String = "digest"): Overlay {
            val dataSourceOverlay = """
        {
          "capture_base": "$captureBase",
          "type": "extend/overlays/data_source/1.0",
          "format": "vc+sd-jwt",
          "attribute_sources" : {
            "$ATTRIBUTE_KEY" : "$ATTRIBUTE_JSON_PATH",
            "$ATTRIBUTE2_KEY" : "$ATTRIBUTE2_JSON_PATH"
          }
        }
            """.trimIndent()

            return json.safeDecodeStringTo<Overlay>(dataSourceOverlay).value
        }

        private fun createDataSourceOverlayV2x0(captureBase: String = "digest"): Overlay {
            val dataSourceOverlay = """
        {
          "capture_base": "$captureBase",
          "type": "extend/overlays/data_source/2.0",
          "format": "vc+sd-jwt",
          "attribute_sources" : {
            "$ATTRIBUTE_KEY" : ["attribute"],
            "$ATTRIBUTE2_KEY" : ["attribute2"]
          }
        }
            """.trimIndent()

            return json.safeDecodeStringTo<Overlay>(dataSourceOverlay).value
        }

        private fun createBrandingOverlay(primaryField: String?, secondaryField: String? = null): Overlay {
            val primaryFieldJson = primaryField?.let { """, "primary_field": "$it" """ } ?: ""
            val secondaryFieldJson = secondaryField?.let { """, "secondary_field": "$it" """ } ?: ""
            val brandingOverlay = """
        {
          "capture_base": "digest",
          "type": "aries/overlays/branding/1.1",
          "language": "en"
          $primaryFieldJson
          $secondaryFieldJson
        }
            """.trimIndent()

            return json.safeDecodeStringTo<Overlay>(brandingOverlay).value
        }

        @JvmStatic
        fun generateInvalidAttributes() = listOf(
            createBrandingOverlay("Primary: {{:attribute}}"),
            createBrandingOverlay("Primary: {{ref:digest:attribute}}"),
            createBrandingOverlay("Primary: {{ref::digest:attribute}}"),
            createBrandingOverlay("Primary: {{ref::digest::attribute}}"),
        )
    }
}
