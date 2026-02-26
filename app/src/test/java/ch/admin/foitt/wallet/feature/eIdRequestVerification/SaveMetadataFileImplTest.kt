package ch.admin.foitt.wallet.feature.eIdRequestVerification

import ch.admin.foitt.avwrapper.AVBeam
import ch.admin.foitt.avwrapper.AVBeamFileData
import ch.admin.foitt.avwrapper.AvBeamFileType
import ch.admin.foitt.avwrapper.sensordata.MetadataFileNames.METADATA_BIN_FILENAME
import ch.admin.foitt.avwrapper.sensordata.MetadataFileNames.METADATA_RECORD_FILENAME
import ch.admin.foitt.avwrapper.sensordata.MetadataFileNames.METADATA_SCAN_FILENAME
import ch.admin.foitt.avwrapper.sensordata.MetadataFileNames.METADATA_SELFIE_FILENAME
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.SaveEIdRequestFileError
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.SaveEIdRequestFiles
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.SaveMetadataFile
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.implementation.SaveMetadataFileImpl
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
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class SaveMetadataFileImplTest {

    @MockK
    private lateinit var mockEIdRequestFile: EIdRequestFile

    @MockK
    private lateinit var mockEIdRequestFileRepository: EIdRequestFileRepository

    @MockK
    private lateinit var mockSaveEIdRequestFiles: SaveEIdRequestFiles

    @MockK
    private lateinit var mockAvBeam: AVBeam

    private val testDispatcher = StandardTestDispatcher()

    lateinit var saveMetadataFile: SaveMetadataFile
    lateinit var eIdRequestFile: EIdRequestFile
    lateinit var avBeamFileData: AVBeamFileData

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        saveMetadataFile = SaveMetadataFileImpl(
            eIdRequestFileRepository = mockEIdRequestFileRepository,
            saveEIdRequestFiles = mockSaveEIdRequestFiles,
            avBeam = mockAvBeam,
            ioDispatcher = testDispatcher
        )

        eIdRequestFile = EIdRequestFile(
            id = 1L,
            eIdRequestCaseId = "123456789",
            fileName = "metadata-record.json",
            mime = "mime",
            category = EIdRequestFileCategory.DOCUMENT_RECORDING,
            data = byteArrayOf(),
            createdAt = Instant.now().epochSecond
        )

        avBeamFileData = AVBeamFileData(
            fileType = AvBeamFileType.UNKNOWN,
            fileDescription = "",
            fileData = byteArrayOf()
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Successfully saving a metadata file returns an Ok`() = runTest(testDispatcher) {
        val fileNotFoundException = Exception("Failed to get EId request file")

        coEvery {
            mockEIdRequestFileRepository.getEIdRequestFileByCaseIdAndFileName(any(), METADATA_BIN_FILENAME)
        } returns Err(EIdRequestError.Unexpected(fileNotFoundException))
        coEvery {
            mockEIdRequestFileRepository.getEIdRequestFileByCaseIdAndFileName(any(), not(eq(METADATA_BIN_FILENAME)))
        } returns Ok(mockEIdRequestFile)
        coEvery { mockEIdRequestFile.fileName } returns ("")
        coEvery { mockEIdRequestFile.data } returns (byteArrayOf())
        coEvery { mockAvBeam.getMetadata(any()) } returns (avBeamFileData)
        coEvery {
            mockSaveEIdRequestFiles.invoke(
                sIdCaseId = any(),
                filesDataList = any(),
                filesCategory = any()
            )
        } returns Ok(Unit)

        saveMetadataFile(caseId = "").assertOk()

        coVerifyOrder {
            mockEIdRequestFileRepository.getEIdRequestFileByCaseIdAndFileName(any(), METADATA_BIN_FILENAME)
            mockEIdRequestFileRepository.getEIdRequestFileByCaseIdAndFileName(any(), any())
            mockAvBeam.getMetadata(any())
            mockSaveEIdRequestFiles.invoke(any(), any(), any())
        }
    }

    @Test
    fun `Error when getting a file from the repository is propagated`() = runTest(testDispatcher) {
        val exception = Exception("error in db")
        val fileNotFoundException = Exception("Failed to get EId request file")

        coEvery {
            mockEIdRequestFileRepository.getEIdRequestFileByCaseIdAndFileName(any(), METADATA_BIN_FILENAME)
        } returns Err(EIdRequestError.Unexpected(fileNotFoundException))
        coEvery {
            mockEIdRequestFileRepository.getEIdRequestFileByCaseIdAndFileName(any(), not(eq(METADATA_BIN_FILENAME)))
        } returns Err(EIdRequestError.Unexpected(exception))

        saveMetadataFile(caseId = "").assertErrorType(SaveEIdRequestFileError::class)

        coVerifyOrder {
            mockEIdRequestFileRepository.getEIdRequestFileByCaseIdAndFileName(any(), METADATA_BIN_FILENAME)
            mockEIdRequestFileRepository.getEIdRequestFileByCaseIdAndFileName(any(), any())
        }

        coVerify(exactly = 0) {
            mockAvBeam.getMetadata(any())
            mockSaveEIdRequestFiles.invoke(any(), any(), any())
        }
    }

    @Test
    fun `Error when getting a non existing mandatory file from the repository`() = runTest(testDispatcher) {
        val fileNotFoundException = Exception("Failed to get EId request file")

        coEvery {
            mockEIdRequestFileRepository.getEIdRequestFileByCaseIdAndFileName(any(), METADATA_BIN_FILENAME)
        } returns Err(EIdRequestError.Unexpected(fileNotFoundException))
        coEvery {
            mockEIdRequestFileRepository.getEIdRequestFileByCaseIdAndFileName(any(), METADATA_SCAN_FILENAME)
        } returns Ok(eIdRequestFile)

        coEvery {
            mockEIdRequestFileRepository.getEIdRequestFileByCaseIdAndFileName(any(), METADATA_RECORD_FILENAME)
        } returns Ok(eIdRequestFile)

        val exception = Exception("error in db")
        coEvery {
            mockEIdRequestFileRepository.getEIdRequestFileByCaseIdAndFileName(any(), METADATA_SELFIE_FILENAME)
        } returns Err(EIdRequestError.Unexpected(exception))

        saveMetadataFile(caseId = "").assertErrorType(SaveEIdRequestFileError::class)

        coVerifyOrder {
            mockEIdRequestFileRepository.getEIdRequestFileByCaseIdAndFileName(any(), METADATA_BIN_FILENAME)
            mockEIdRequestFileRepository.getEIdRequestFileByCaseIdAndFileName(any(), any())
        }

        coVerify(exactly = 0) {
            mockAvBeam.getMetadata(any())
            mockSaveEIdRequestFiles.invoke(any(), any(), any())
        }
    }

    @Test
    fun `Successfully when getting a non mandatory file from the repository`() = runTest(testDispatcher) {
        val fileNotFoundException = Exception("Failed to get EId request file")

        coEvery {
            mockEIdRequestFileRepository.getEIdRequestFileByCaseIdAndFileName(any(), METADATA_BIN_FILENAME)
        } returns Err(EIdRequestError.Unexpected(fileNotFoundException))
        coEvery {
            mockEIdRequestFileRepository.getEIdRequestFileByCaseIdAndFileName(any(), METADATA_SCAN_FILENAME)
        } returns Ok(eIdRequestFile)

        coEvery {
            mockEIdRequestFileRepository.getEIdRequestFileByCaseIdAndFileName(any(), METADATA_SELFIE_FILENAME)
        } returns Ok(eIdRequestFile)

        val exception = Exception("error in db")
        coEvery {
            mockEIdRequestFileRepository.getEIdRequestFileByCaseIdAndFileName(any(), METADATA_RECORD_FILENAME)
        } returns Err(EIdRequestError.Unexpected(exception))

        coEvery { mockEIdRequestFile.fileName } returns ("")
        coEvery { mockEIdRequestFile.data } returns (byteArrayOf())
        coEvery { mockAvBeam.getMetadata(any()) } returns (avBeamFileData)
        coEvery {
            mockSaveEIdRequestFiles.invoke(
                sIdCaseId = any(),
                filesDataList = any(),
                filesCategory = any()
            )
        } returns Ok(Unit)

        saveMetadataFile(caseId = "").assertOk()

        coVerifyOrder {
            mockEIdRequestFileRepository.getEIdRequestFileByCaseIdAndFileName(any(), METADATA_BIN_FILENAME)
            mockEIdRequestFileRepository.getEIdRequestFileByCaseIdAndFileName(any(), any())
            mockAvBeam.getMetadata(any())
            mockSaveEIdRequestFiles.invoke(any(), any(), any())
        }
    }

    @Test
    fun `Error when getting metadata from lib`() = runTest(testDispatcher) {
        val fileNotFoundException = Exception("Failed to get EId request file")
        val metadataException = Exception("AVBeam failed to generate metadata")

        coEvery {
            mockEIdRequestFileRepository.getEIdRequestFileByCaseIdAndFileName(any(), METADATA_BIN_FILENAME)
        } returns Err(EIdRequestError.Unexpected(fileNotFoundException))
        coEvery {
            mockEIdRequestFileRepository.getEIdRequestFileByCaseIdAndFileName(any(), not(eq(METADATA_BIN_FILENAME)))
        } returns Ok(mockEIdRequestFile)
        coEvery { mockEIdRequestFile.fileName } returns ("")
        coEvery { mockEIdRequestFile.data } returns (byteArrayOf())
        coEvery { mockAvBeam.getMetadata(any()) } throws metadataException
        coEvery {
            mockSaveEIdRequestFiles.invoke(
                sIdCaseId = any(),
                filesDataList = any(),
                filesCategory = any()
            )
        } returns Ok(Unit)

        saveMetadataFile(caseId = "").assertOk()

        coVerifyOrder {
            mockEIdRequestFileRepository.getEIdRequestFileByCaseIdAndFileName(any(), METADATA_BIN_FILENAME)
            mockEIdRequestFileRepository.getEIdRequestFileByCaseIdAndFileName(any(), any())
            mockAvBeam.getMetadata(any())
        }

        coVerify(exactly = 0) {
            mockSaveEIdRequestFiles.invoke(any(), any(), any())
        }
    }
}
