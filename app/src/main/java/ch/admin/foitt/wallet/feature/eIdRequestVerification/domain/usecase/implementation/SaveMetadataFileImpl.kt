package ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.implementation

import ch.admin.foitt.avwrapper.AVBeam
import ch.admin.foitt.avwrapper.AVBeamFilesDataList
import ch.admin.foitt.avwrapper.sensordata.MetadataFileNames.METADATA_BIN_FILENAME
import ch.admin.foitt.avwrapper.sensordata.MetadataFileNames.METADATA_RECORD_FILENAME
import ch.admin.foitt.avwrapper.sensordata.MetadataFileNames.METADATA_SCAN_FILENAME
import ch.admin.foitt.avwrapper.sensordata.MetadataFileNames.METADATA_SELFIE_FILENAME
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.EIdRequestVerificationError
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.SaveEIdRequestFileError
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.toSaveEIdRequestFileError
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.SaveEIdRequestFiles
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.SaveMetadataFile
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestFileCategory
import ch.admin.foitt.wallet.platform.di.IoDispatcher
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestFileRepository
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class SaveMetadataFileImpl @Inject constructor(
    private val eIdRequestFileRepository: EIdRequestFileRepository,
    private val saveEIdRequestFiles: SaveEIdRequestFiles,
    private val avBeam: AVBeam,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : SaveMetadataFile {
    override suspend fun invoke(caseId: String): Result<Unit, SaveEIdRequestFileError> = withContext(ioDispatcher) {
        coroutineBinding {
            val listOfMetadataJsonFiles = mutableListOf<ByteArray>()

            val metaDataExists = eIdRequestFileRepository
                .getEIdRequestFileByCaseIdAndFileName(caseId, METADATA_BIN_FILENAME)
                .isOk

            if (metaDataExists) {
                return@coroutineBinding
            }

            for ((filename, isMandatory) in metadataJsonFiles) {
                val fileResult = eIdRequestFileRepository.getEIdRequestFileByCaseIdAndFileName(caseId, filename)
                    .mapError {
                        it.toSaveEIdRequestFileError()
                    }.getOrElse {
                        if (!isMandatory) {
                            Timber.d("Skipping optional file '$filename' (not found)")
                            continue
                        }
                        Err(it).bind()
                    }
                listOfMetadataJsonFiles.add(fileResult.data)
            }
            runSuspendCatching {
                val metadataFile = avBeam.getMetadata(listOfMetadataJsonFiles)
                val metadataList = AVBeamFilesDataList(value = listOf(metadataFile))

                saveEIdRequestFiles(
                    sIdCaseId = caseId,
                    filesDataList = metadataList,
                    filesCategory = EIdRequestFileCategory.METADATA,
                ).onFailure { error ->
                    Timber.d("Metadata file: error saving file $error")
                }.onSuccess {
                    Timber.d("Metadata file: success saving file")
                }.bind()
            }.getOrElse { error ->
                Err(EIdRequestVerificationError.Unexpected(error))
            }
        }
    }

    private val metadataJsonFiles: HashMap<String, Boolean> = hashMapOf(
        METADATA_SCAN_FILENAME to true,
        METADATA_RECORD_FILENAME to false,
        METADATA_SELFIE_FILENAME to true
    )
}
