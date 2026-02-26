package ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.FileUploadConfig
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.UploadAllFiles
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestFile
import ch.admin.foitt.wallet.platform.di.IoDispatcher
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.AvUploadFilesError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.UploadFileRequest
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.toAvUploadFilesError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestFileRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.UploadFileToCase
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.getError
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.mapError
import io.ktor.http.ContentType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class UploadAllFilesImpl @Inject constructor(
    private val eIdRequestFileRepository: EIdRequestFileRepository,
    private val uploadFileToCase: UploadFileToCase,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UploadAllFiles {
    override suspend fun invoke(
        caseId: String,
        accessToken: String
    ): Result<Unit, AvUploadFilesError> = withContext(ioDispatcher) {
        coroutineBinding {
            filesToUpload.map { fileToUpload ->
                async {
                    val file = getEIdRequestFile(caseId, fileToUpload.fileName).getOrElse {
                        if (!fileToUpload.isMandatory) {
                            Timber.d("Skipping optional file '${fileToUpload.fileName}' (not found)")
                            return@async Ok(Unit)
                        }
                        Err(it).bind()
                    }

                    retryWithLimit(NUMBER_RETRIES) {
                        uploadFileToCase(
                            UploadFileRequest(
                                caseId = caseId,
                                accessToken = accessToken,
                                fileName = fileToUpload.serverFileName,
                                document = file.data,
                                mime = fileToUpload.contentType
                            )
                        ).mapError { it.toAvUploadFilesError() }
                    }.bind()
                }
            }.awaitAll()
        }
    }

    private suspend fun getEIdRequestFile(
        caseId: String,
        fileName: String
    ): Result<EIdRequestFile, AvUploadFilesError> = eIdRequestFileRepository.getEIdRequestFileByCaseIdAndFileName(caseId, fileName)
        .mapError {
            EIdRequestError.FileNotFound(fileName)
        }

    private suspend fun <T> retryWithLimit(
        times: Int = 3,
        block: suspend () -> Result<T, AvUploadFilesError>
    ): Result<T, AvUploadFilesError> {
        var lastError: AvUploadFilesError? = null
        repeat(times) { attempt ->
            val result = block()
            if (result.isOk) {
                return result
            } else {
                lastError = result.getError()
                if (attempt < times - 1) delay(DELAY_BEFORE_UPLOAD_AGAIN)
            }
        }
        return Err(lastError ?: EIdRequestError.Unexpected(null))
    }

    private val filesToUpload: List<FileUploadConfig> = listOf(
        FileUploadConfig(FIRST_PAGE, ContentType.Image.PNG, FIRST_PAGE, true),
        FileUploadConfig(SECOND_PAGE, ContentType.Image.PNG, SECOND_PAGE, true),
        FileUploadConfig(VIDEO, ContentType.Video.MP4, VIDEO, true),
        FileUploadConfig(METADATA, ContentType.Application.OctetStream, METADATA, false),
        // optional unless docVideoRequired was true during case creation
        FileUploadConfig(DOCUMENT, ContentType.Video.MP4, DOCUMENT_SERVER_NAME, false),
        FileUploadConfig(MOBILE_RESULT_XML, ContentType.Application.Xml, MOBILE_RESULT_XML_SERVER_NAME, true),
        FileUploadConfig(MOBILE_RESULT_JSON, ContentType.Application.Json, MOBILE_RESULT_JSON_SERVER_NAME, true),
    )

    companion object {
        private const val FIRST_PAGE = "fullFrameFirstPage.png"
        private const val SECOND_PAGE = "fullFrameSecondPage.png"
        private const val VIDEO = "video.mp4"
        private const val METADATA = "metadata.bin"
        private const val DOCUMENT = "docRecVideo.mp4"
        private const val DOCUMENT_SERVER_NAME = "document.mp4"
        private const val MOBILE_RESULT_JSON = "result.json"
        private const val MOBILE_RESULT_JSON_SERVER_NAME = "mobile-result.json"
        private const val MOBILE_RESULT_XML = "result.xml"
        private const val MOBILE_RESULT_XML_SERVER_NAME = "mobile-result.xml"
        private const val NUMBER_RETRIES = 3
        private const val DELAY_BEFORE_UPLOAD_AGAIN = 500L
    }
}
