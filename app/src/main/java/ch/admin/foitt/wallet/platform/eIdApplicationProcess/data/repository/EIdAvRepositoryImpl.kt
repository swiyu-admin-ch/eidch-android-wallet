package ch.admin.foitt.wallet.platform.eIdApplicationProcess.data.repository

import ch.admin.foitt.openid4vc.di.ExternalOpenId4VcModule.Companion.NAMED_DEFAULT_HTTP_CLIENT
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.AvRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.UploadFileRequest
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.toAvRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdAvRepository
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentLength
import io.ktor.http.contentType
import javax.inject.Inject
import javax.inject.Named

class EIdAvRepositoryImpl @Inject constructor(
    @param:Named(NAMED_DEFAULT_HTTP_CLIENT) private val httpClient: HttpClient,
    private val environmentSetupRepository: EnvironmentSetupRepository
) : EIdAvRepository {
    override suspend fun uploadFileToCase(
        file: UploadFileRequest
    ): Result<Unit, AvRepositoryError> = runSuspendCatching<Unit> {
        httpClient.post(environmentSetupRepository.avBackendUrl + "cases/v1/${file.caseId}/files") {
            header(HttpHeaders.Authorization, "Bearer ${file.accessToken}")
            header(HttpHeaders.ContentDisposition, "attachment; filename=\"${file.fileName}\"")
            contentLength()
            contentType(ContentType.Application.OctetStream)
            setBody(file.document)
        }
    }.mapError { throwable ->
        throwable.toAvRepositoryError("uploadFileToCase error")
    }

    override suspend fun submitCase(
        caseId: String,
        accessToken: String,
    ): Result<Unit, AvRepositoryError> = runSuspendCatching<Unit> {
        httpClient.post(environmentSetupRepository.avBackendUrl + "cases/v1/$caseId/submit") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
    }.mapError { throwable ->
        throwable.toAvRepositoryError("submit case error")
    }
}
