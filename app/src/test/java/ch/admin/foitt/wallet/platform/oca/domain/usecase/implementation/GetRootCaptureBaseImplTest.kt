package ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.oca.domain.model.AttributeType
import ch.admin.foitt.wallet.platform.oca.domain.model.CaptureBase1x0
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaError
import ch.admin.foitt.wallet.platform.oca.domain.usecase.GetRootCaptureBase
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.elfaExample
import ch.admin.foitt.wallet.util.SafeJsonTestInstance
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.annotation.UnsafeResultValueAccess
import io.mockk.MockKAnnotations
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetRootCaptureBaseImplTest {

    private val json = SafeJsonTestInstance.safeJson

    private lateinit var useCase: GetRootCaptureBase

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        useCase = GetRootCaptureBaseImpl()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @OptIn(UnsafeResultValueAccess::class)
    @Test
    fun `Capture bases are validated successfully`(): Unit = runTest {
        val bundle = json.safeDecodeStringTo<OcaBundle>(elfaExample).value

        useCase(bundle.captureBases).assertOk()
    }

    @Test
    fun `0 root capture bases returns an error`(): Unit = runTest {
        useCase(captureBasesWithoutRootCaptureBase).assertErrorType(OcaError.InvalidRootCaptureBase::class)
    }

    @Test
    fun `Multiple root capture bases returns an error`(): Unit = runTest {
        useCase(captureBasesWithMultipleRootCaptureBases).assertErrorType(OcaError.InvalidRootCaptureBase::class)
    }

    private val captureBasesWithoutRootCaptureBase = listOf(
        CaptureBase1x0(
            digest = "validDigest",
            attributes = mapOf(
                "attributeKey" to AttributeType.Text,
                "attributeKey2" to AttributeType.Reference("validDigest"),
            )
        ),
    )

    private val captureBasesWithMultipleRootCaptureBases = listOf(
        CaptureBase1x0(
            digest = "validDigest",
            attributes = mapOf(
                "attributeKey" to AttributeType.Text,
            )
        ),
        CaptureBase1x0(
            digest = "validDigest2",
            attributes = mapOf(
                "attributeKey" to AttributeType.Text,
            )
        ),
    )
}
