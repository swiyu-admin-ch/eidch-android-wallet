package ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.implementation

import ch.admin.foitt.avwrapper.AVBeamFileData
import ch.admin.foitt.avwrapper.AVBeamFilesDataList
import ch.admin.foitt.avwrapper.AvBeamFileType
import ch.admin.foitt.avwrapper.DocumentScanPackageResult
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.GetDocumentScanDataError
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.toGetDocumentScanDataError
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.GetDocumentScanData
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.GetEIdMrzValues
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestFileRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestFileRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import timber.log.Timber
import javax.inject.Inject

class GetDocumentScanDataImpl @Inject constructor(
    private val eIdRequestFileRepository: EIdRequestFileRepository,
    private val getEIdMrzValues: GetEIdMrzValues,
) : GetDocumentScanData {
    override suspend fun invoke(
        caseId: String
    ): Result<DocumentScanPackageResult, GetDocumentScanDataError> = coroutineBinding {
        val serializedDataList = eIdRequestFileRepository.getEIdRequestFileByCaseIdAndFileName(
            caseId = caseId,
            fileName = DocumentScanPackageResult.FILE_EXTRACT_DATA_LIST,
        ).map { requestFile ->
            requestFile.data.decodeToString()
        }.mapError(EIdRequestFileRepositoryError::toGetDocumentScanDataError).bind()

        val mrzValues = getEIdMrzValues(serializedDataList).bind()

        val nfcFileList: List<AVBeamFileData> = eIdRequestFileRepository.getEIdRequestFilesByCaseId(caseId).map { dbFileList ->
            dbFileList
                .filter { dbFile ->
                    dbFile.fileName != DocumentScanPackageResult.FILE_EXTRACT_DATA_LIST
                }.map { dbFile ->
                    AVBeamFileData(
                        fileDescription = dbFile.fileName,
                        fileType = AvBeamFileType.fromMimeType(dbFile.mime),
                        fileData = dbFile.data,
                    )
                }
        }.mapError(EIdRequestFileRepositoryError::toGetDocumentScanDataError).bind()

        Timber.d("NfcScan: getDocumentScanPackageResult $serializedDataList")
        DocumentScanPackageResult(
            serializedDataList = serializedDataList,
            mrzValues = mrzValues,
            files = AVBeamFilesDataList(nfcFileList)
        )
    }
}
