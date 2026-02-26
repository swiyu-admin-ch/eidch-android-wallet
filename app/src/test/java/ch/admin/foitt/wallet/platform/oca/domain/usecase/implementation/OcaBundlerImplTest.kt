package ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.oca.domain.model.CaptureBase
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaError
import ch.admin.foitt.wallet.platform.oca.domain.model.OverlaySpecType
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.Overlay
import ch.admin.foitt.wallet.platform.oca.domain.usecase.GenerateOcaClaimData
import ch.admin.foitt.wallet.platform.oca.domain.usecase.GenerateOcaCredentialData
import ch.admin.foitt.wallet.platform.oca.domain.usecase.GetRootCaptureBase
import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaBundler
import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaCaptureBaseValidator
import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaCesrHashValidator
import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaOverlayValidator
import ch.admin.foitt.wallet.platform.oca.domain.usecase.TransformOcaOverlays
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.betaIdExample
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.elfaCaptureBase
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.elfaExample
import ch.admin.foitt.wallet.util.SafeJsonTestInstance
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.annotation.UnsafeResultValueAccess
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class OcaBundlerImplTest {

    @MockK
    private lateinit var mockOcaCesrHashValidator: OcaCesrHashValidator

    @MockK
    private lateinit var mockOcaCaptureBaseValidator: OcaCaptureBaseValidator

    @MockK
    private lateinit var mockOcaOverlayValidator: OcaOverlayValidator

    @MockK
    private lateinit var mockTransformOcaOverlays: TransformOcaOverlays

    @MockK
    private lateinit var mockGetRootCaptureBase: GetRootCaptureBase

    @MockK
    private lateinit var mockGenerateOcaClaimData: GenerateOcaClaimData

    @MockK
    private lateinit var mockGenerateOcaCredentialData: GenerateOcaCredentialData

    private val json = SafeJsonTestInstance.safeJson
    private lateinit var ocaBundler: OcaBundler

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        ocaBundler = OcaBundlerImpl(
            json = json,
            ocaCesrHashValidator = mockOcaCesrHashValidator,
            ocaCaptureBaseValidator = mockOcaCaptureBaseValidator,
            ocaOverlayValidator = mockOcaOverlayValidator,
            transformOcaOverlays = mockTransformOcaOverlays,
            getRootCaptureBase = mockGetRootCaptureBase,
            generateOcaClaimData = mockGenerateOcaClaimData,
            generateOcaCredentialData = mockGenerateOcaCredentialData,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @TestFactory
    fun `Oca with valid structure is processed without error`(): List<DynamicTest> {
        return mapOf("elfaJson" to elfaExample, "betaIdJson" to betaIdExample).map { (name, ocaJson) ->
            DynamicTest.dynamicTest(name) {
                runTest {
                    ocaBundler(ocaJson).assertOk()
                }
            }
        }
    }

    @Test
    fun `Invalid decoding of oca string to json object returns an error`(): Unit = runTest {
        ocaBundler("not a json object").assertErrorType(OcaError.InvalidJsonObject::class)
    }

    @Test
    fun `Oca json not containing a capture_bases array returns an error`(): Unit = runTest {
        ocaBundler(ocaBundleWithoutCaptureBases).assertErrorType(OcaError.InvalidJsonObject::class)
    }

    @OptIn(UnsafeResultValueAccess::class)
    @Test
    fun `Failing CESR validation returns an error`(): Unit = runTest {
        coEvery {
            mockOcaCesrHashValidator(json.safeDecodeStringTo<JsonObject>(elfaCaptureBase).value.toString())
        } returns Err(OcaError.InvalidCESRHash("message"))

        ocaBundler(elfaExample).assertErrorType(OcaError.InvalidCESRHash::class)
    }

    @Test
    fun `Oca that can not be decoded to json object returns an error`() = runTest {
        ocaBundler(ocaBundleNotParsable).assertErrorType(OcaError.InvalidJsonObject::class)
    }

    @Test
    fun `Oca that contains unsupported overlays returns the bundle without those`() = runTest {
        val result = ocaBundler(ocaBundleWithUnsupportedOverlay).assertOk()

        val overlays = result.overlays
        assertEquals(1, overlays.size)
        assertEquals(OverlaySpecType.LABEL_1_0, overlays[0].type)
    }

    @Test
    fun `Oca bundler maps errors from oca capture base validator`() = runTest {
        coEvery { mockOcaCaptureBaseValidator(any()) } returns Err(OcaError.InvalidRootCaptureBase)

        ocaBundler(elfaExample).assertErrorType(OcaError.InvalidCaptureBases::class)
    }

    @Test
    fun `Oca bundler maps errors from oca overlay validator`() = runTest {
        coEvery { mockOcaOverlayValidator(any()) } returns Err(OcaError.MissingMandatoryOverlay)

        ocaBundler(elfaExample).assertErrorType(OcaError.InvalidOverlays::class)
    }

    @Test
    fun `Oca bundler maps errors from getting root capture base`() = runTest {
        coEvery { mockGetRootCaptureBase(any()) } returns Err(OcaError.InvalidRootCaptureBase)

        ocaBundler(elfaExample).assertErrorType(OcaError.InvalidCaptureBases::class)
    }

    @OptIn(UnsafeResultValueAccess::class)
    private fun setupDefaultMocks() {
        val captureBase = json.safeDecodeStringTo<CaptureBase>(defaultCaptureBase).value
        val captureBases = listOf(captureBase)
        val overlays = listOf(json.safeDecodeStringTo<Overlay>(defaultOverlay).value)

        coEvery { mockOcaCesrHashValidator(any()) } returns Ok(Unit)
        coEvery { mockOcaCaptureBaseValidator(any()) } returns Ok(captureBases)
        coEvery { mockOcaOverlayValidator(any()) } returns Ok(overlays)
        coEvery { mockTransformOcaOverlays(any()) } returns overlays
        coEvery { mockGetRootCaptureBase(any()) } returns Ok(captureBase)
        coEvery { mockGenerateOcaClaimData(any(), any()) } returns emptyList()
        coEvery { mockGenerateOcaCredentialData(any(), any()) } returns emptyList()
    }

    private val defaultCaptureBase = """
        {
          "type": "spec/capture_base/1.0",
          "digest": "digest",
          "attributes": {
            "attributeKey": "Text"
          }
        }
    """.trimIndent()

    private val defaultOverlay = """
        {
          "capture_base": "digest",
          "type": "spec/overlays/label/1.0",
          "language": "en",
          "attribute_labels" : {
            "attributeKey" : "label"
          }
        }
    """.trimIndent()

    private val ocaBundleWithoutCaptureBases = """
        {
          "overlays": [
            $defaultOverlay
          ]
        }
    """.trimIndent()

    private val ocaBundleNotParsable = """
        {
          "capture_bases": [
            $defaultCaptureBase
          ]
        }
    """.trimIndent()

    private val ocaBundleWithUnsupportedOverlay = """
        {
          "capture_bases": [
            $defaultCaptureBase
          ],
          "overlays": [
            {
              "capture_base": "digest",
              "type": "spec/overlays/unsupported/1.0",
              "someKey": "someValue"
            },
            $defaultOverlay
          ]
        }
    """.trimIndent()
}
