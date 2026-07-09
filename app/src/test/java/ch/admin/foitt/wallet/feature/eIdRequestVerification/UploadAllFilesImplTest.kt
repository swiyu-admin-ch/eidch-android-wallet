package ch.admin.foitt.wallet.feature.eIdRequestVerification

import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.UploadAllFiles
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.implementation.UploadAllFilesImpl
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestFile
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestFileRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.UploadFileToCase
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UploadAllFilesImplTest {

    @MockK
    private lateinit var mockEIdRequestFile: EIdRequestFile

    @MockK
    private lateinit var mockEIdRequestFileRepository: EIdRequestFileRepository

    @MockK
    private lateinit var mockUploadFileToCase: UploadFileToCase

    private val testDispatcher = StandardTestDispatcher()

    lateinit var uploadAllFiles: UploadAllFiles

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        uploadAllFiles = UploadAllFilesImpl(
            mockEIdRequestFileRepository,
            mockUploadFileToCase,
            testDispatcher
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Successfully getting a file by caseId returns an Ok`() = runTest(testDispatcher) {
        coEvery {
            mockEIdRequestFileRepository.getEIdRequestFileByCaseIdAndFileName(any(), any())
        } returns Ok(mockEIdRequestFile)
        coEvery { mockEIdRequestFile.fileName } returns ("")
        coEvery { mockEIdRequestFile.data } returns (byteArrayOf())

        coEvery {
            mockUploadFileToCase.invoke(any())
        } returns Ok(Unit)

        val finalProgress = uploadAllFiles(caseId = "", accessToken = "").toList().last().assertOk()
        assertEquals(finalProgress.total, finalProgress.completed)
    }

    @Test
    fun `Error when getting a file from the repository is propagated`() = runTest(testDispatcher) {
        val exception = Exception("error in db")
        coEvery {
            mockEIdRequestFileRepository.getEIdRequestFileByCaseIdAndFileName(any(), any())
        } returns Err(EIdRequestError.Unexpected(exception))

        uploadAllFiles(caseId = "", accessToken = "").toList().last().assertErrorType(EIdRequestError.FileNotFound::class)
    }

    @Test
    fun `Optional files not found are skipped and upload completes successfully`() = runTest(testDispatcher) {
        coEvery {
            mockEIdRequestFileRepository.getEIdRequestFileByCaseIdAndFileName(
                any(),
                match { it == "metadata.bin" || it == "docRecVideo.mp4" }
            )
        } returns Err(EIdRequestError.Unexpected(Exception("not found")))
        coEvery {
            mockEIdRequestFileRepository.getEIdRequestFileByCaseIdAndFileName(
                any(),
                match { it != "metadata.bin" && it != "docRecVideo.mp4" }
            )
        } returns Ok(mockEIdRequestFile)
        coEvery { mockEIdRequestFile.fileName } returns ""
        coEvery { mockEIdRequestFile.data } returns byteArrayOf()
        coEvery { mockUploadFileToCase.invoke(any()) } returns Ok(Unit)

        val finalProgress = uploadAllFiles(caseId = "", accessToken = "").toList().last().assertOk()
        assertEquals(finalProgress.total, finalProgress.completed)
    }

    @Test
    fun `Upload failure emits partial progress then error, never reaching 1_0f`() = runTest(testDispatcher) {
        coEvery {
            mockEIdRequestFileRepository.getEIdRequestFileByCaseIdAndFileName(any(), any())
        } returns Ok(mockEIdRequestFile)
        coEvery { mockEIdRequestFile.fileName } returns ""
        coEvery { mockEIdRequestFile.data } returns byteArrayOf()
        coEvery { mockUploadFileToCase.invoke(any()) } returns Ok(Unit)
        coEvery {
            mockUploadFileToCase.invoke(match { it.fileName == "video.mp4" })
        } returns Err(EIdRequestError.Unexpected(Exception("upload failed")))

        val emissions = uploadAllFiles(caseId = "", accessToken = "").toList()

        assertTrue(emissions.dropLast(1).all { it.isOk })
        emissions.last().assertErrorType(EIdRequestError.Unexpected::class)
        val progressValues = emissions.filter { it.isOk }.map { it.assertOk() }
        assertFalse(progressValues.any { it.completed == it.total })
    }
}
