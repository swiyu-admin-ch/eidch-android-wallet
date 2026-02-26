package ch.admin.foitt.wallet.feature.eIdRequestVerification

import ch.admin.foitt.avwrapper.DocumentScanPackageResult
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.EIdRequestVerificationError
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.GetDocumentScanData
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.implementation.AreEIdDocumentsEqualImpl
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AreEIdDocumentsEqualImplTest {

    @MockK
    private lateinit var mockGetDocumentData: GetDocumentScanData
    private lateinit var useCase: AreEIdDocumentsEqualImpl
    val fakePackageData = mockk<DocumentScanPackageResult>(relaxed = true)

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { mockGetDocumentData(any()) } returns Ok(fakePackageData)
        useCase = AreEIdDocumentsEqualImpl(mockGetDocumentData)
    }

    @Test
    fun `should return true when caseId is empty`() = runTest {
        val caseId = ""
        val newDocument = arrayOf("val1", "val2")

        useCase(caseId, newDocument).assertOk()
    }

    @Test
    fun `should return error when getDocumentData fails`() = runTest {
        val exception = IllegalStateException("error with the files")
        val caseId = "case123"

        coEvery {
            mockGetDocumentData(caseId)
        } returns Err(EIdRequestVerificationError.Unexpected(exception))

        useCase(caseId, arrayOf("val1")).assertErrorType(EIdRequestVerificationError.Unexpected::class)
    }

    @Test
    fun `should return true when new document matches previous document mrzValues`() = runTest {
        val caseId = "case123"
        val mrzList = listOf("ABC", "123")
        val newDocument = arrayOf("ABC", "123")

        val mockScanData = mockk<DocumentScanPackageResult> {
            every { mrzValues } returns mrzList
        }

        coEvery { mockGetDocumentData(caseId) } returns Ok(mockScanData)

        val result = useCase(caseId, newDocument).assertOk()
        assertTrue(result)
    }

    @Test
    fun `should return false when documents do not match`() = runTest {
        val caseId = "case123"
        val mrzList = listOf("ABC", "123")
        val newDocument = arrayOf("XYZ", "999")

        val mockScanData = mockk<DocumentScanPackageResult> {
            every { mrzValues } returns mrzList
        }

        coEvery { mockGetDocumentData(caseId) } returns Ok(mockScanData)

        val result = useCase(caseId, newDocument).assertOk()
        assertFalse(result)
    }
}
