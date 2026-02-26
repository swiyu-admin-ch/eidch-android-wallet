package ch.admin.foitt.wallet.feature.eIdRequestVerification

import ch.admin.foitt.avwrapper.DocumentScanPackageResult
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.EIdRequestVerificationError
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.GetEIdMrzValues
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.implementation.GetDocumentScanDataImpl
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestFile
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestFileCategory
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestFileRepository
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetDocumentScanDataImplTest {

    @MockK
    private lateinit var mockEIdRequestFileRepository: EIdRequestFileRepository

    @MockK
    private lateinit var mockGetEIMrzValues: GetEIdMrzValues

    val testCaseId = "testCaseId"
    val testFileName = DocumentScanPackageResult.FILE_EXTRACT_DATA_LIST
    val testFileData = byteArrayOf(1, 2, 2, 50)
    val testResultFile = EIdRequestFile(
        id = 1,
        eIdRequestCaseId = testCaseId,
        fileName = testFileName,
        data = testFileData,
        mime = "someMime",
        category = EIdRequestFileCategory.DOCUMENT_SCAN,
        createdAt = 0,
    )

    val testResultFileList = listOf(
        testResultFile.copy(id = 2, fileName = "testFileName2"),
        testResultFile,
        testResultFile.copy(id = 3, fileName = "testFileName3"),
    )

    lateinit var useCase: GetDocumentScanDataImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { mockEIdRequestFileRepository.getEIdRequestFileByCaseIdAndFileName(any(), any()) } returns Ok(testResultFile)
        coEvery { mockEIdRequestFileRepository.getEIdRequestFilesByCaseId(any()) } returns Ok(testResultFileList)
        coEvery { mockGetEIMrzValues(any()) } returns Ok(emptyList())

        useCase = GetDocumentScanDataImpl(
            eIdRequestFileRepository = mockEIdRequestFileRepository,
            getEIdMrzValues = mockGetEIMrzValues,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Successfully getting document scan data returns a DocumentScanPackageResult`() = runTest {
        val result = useCase(testCaseId)
        result.assertOk()

        coVerifyOrder {
            mockEIdRequestFileRepository.getEIdRequestFileByCaseIdAndFileName(testCaseId, testFileName)
            mockEIdRequestFileRepository.getEIdRequestFilesByCaseId(testCaseId)
        }
    }

    @Test
    fun `A Successful result returns a properly populated package`() = runTest {
        val result = useCase(testCaseId)
        val packageResult = result.assertOk()

        assertEquals(2, packageResult.files?.value?.size,)
        assert(
            packageResult.files?.value?.any {
                it.fileDescription != DocumentScanPackageResult.FILE_EXTRACT_DATA_LIST
            } == true
        )
        assertEquals(testFileData.decodeToString(), packageResult.serializedDataList)
    }

    @Test
    fun `A missing dataList error is propagated`() = runTest {
        val exception = Exception("myError")
        coEvery {
            mockEIdRequestFileRepository.getEIdRequestFileByCaseIdAndFileName(any(), any())
        } returns Err(EIdRequestError.Unexpected(exception))

        val result = useCase(testCaseId)
        val error = result.assertErrorType(EIdRequestVerificationError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    @Test
    fun `A repository failure is propagated`() = runTest {
        val exception = Exception("myError")
        coEvery {
            mockEIdRequestFileRepository.getEIdRequestFilesByCaseId(any())
        } returns Err(EIdRequestError.Unexpected(exception))

        val result = useCase(testCaseId)
        val error = result.assertErrorType(EIdRequestVerificationError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    @Test
    fun `A GetEIdMrzValues fails and is propagated`() = runTest {
        val errorFromUseCase = EIdRequestVerificationError.Unexpected(Exception("No MRZ values found in JSON"))
        coEvery { mockGetEIMrzValues(any()) } returns Err(errorFromUseCase)

        val result = useCase(testCaseId)
        val error = result.assertErrorType(EIdRequestVerificationError.Unexpected::class)
        assertEquals(errorFromUseCase, error)
    }
}
