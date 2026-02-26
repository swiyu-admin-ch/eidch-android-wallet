package ch.admin.foitt.openid4vc.data

import ch.admin.foitt.openid4vc.di.ExternalOpenId4VcModule.Companion.NAMED_DEFAULT_HTTP_CLIENT
import ch.admin.foitt.openid4vc.domain.model.HttpErrorBody
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.FetchPresentationRequestError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestErrorBody
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestType
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.SubmitAnyCredentialPresentationError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.SubmitPresentationErrorError
import ch.admin.foitt.openid4vc.domain.repository.PresentationRequestRepository
import ch.admin.foitt.openid4vc.utils.JsonError
import ch.admin.foitt.openid4vc.utils.JsonParsingError
import ch.admin.foitt.openid4vc.utils.SafeJson
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapBoth
import com.github.michaelbull.result.mapError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.parameters
import java.io.IOException
import java.net.URL
import javax.inject.Inject
import javax.inject.Named

internal class PresentationRequestRepositoryImpl @Inject constructor(
    @param:Named(NAMED_DEFAULT_HTTP_CLIENT) private val httpClient: HttpClient,
    private val safeJson: SafeJson,
) : PresentationRequestRepository {
    override suspend fun fetchPresentationRequest(url: URL) =
        runSuspendCatching<String> {
            httpClient.get(url) {
                contentType(ContentType.Application.Json)
            }.body()
        }.mapError(Throwable::toFetchPresentationRequestError)

    override suspend fun submitPresentation(
        url: URL,
        presentationRequestType: PresentationRequestType,
    ): Result<Unit, SubmitAnyCredentialPresentationError> = coroutineBinding {
        runSuspendCatching {
            when (presentationRequestType) {
                is PresentationRequestType.Json -> httpClient.submitForm(
                    url = url.toExternalForm(),
                    formParameters = parameters {
                        append("vp_token", presentationRequestType.vpToken)
                        append("presentation_submission", presentationRequestType.presentationSubmission)
                    }
                )
                is PresentationRequestType.Jwt -> httpClient.submitForm(
                    url = url.toExternalForm(),
                    formParameters = parameters {
                        append("response", presentationRequestType.response)
                    }
                )
            }
        }.mapError { throwable ->
            when (throwable) {
                is ClientRequestException -> handleClientRequestException(throwable)
                else -> throwable.toSubmitAnyCredentialPresentationError()
            }
        }.bind()
    }

    override suspend fun submitPresentationError(
        url: String,
        body: PresentationRequestErrorBody,
    ) = runSuspendCatching<Unit> {
        httpClient.submitForm(
            url = url,
            formParameters = parameters {
                append("error", body.error.key)
                body.errorDescription?.let { append("error_description", body.errorDescription) }
            }
        )
    }.mapError(Throwable::toSubmitPresentationErrorError)

    private suspend fun handleClientRequestException(clientRequestException: ClientRequestException): SubmitAnyCredentialPresentationError =
        when (clientRequestException.response.status) {
            HttpStatusCode.BadRequest -> parseError(clientRequestException)
            else -> PresentationRequestError.Unexpected(clientRequestException)
        }

    private suspend fun parseError(clientRequestException: ClientRequestException): SubmitAnyCredentialPresentationError {
        val errorBodyString = clientRequestException.response.bodyAsText()
        val errorBodyResult = safeJson.safeDecodeStringTo<HttpErrorBody>(errorBodyString)
        return errorBodyResult.mapBoth(
            success = {
                when {
                    it.isValidationError() -> PresentationRequestError.ValidationError
                    it.isVerificationError() -> PresentationRequestError.VerificationError
                    it.isInvalidCredentialError() -> PresentationRequestError.InvalidCredentialError
                    else -> PresentationRequestError.Unexpected(clientRequestException)
                }
            },
            failure = JsonParsingError::toSubmitPresentationError
        )
    }

    private fun HttpErrorBody.isValidationError() = this.error in ERRORS

    private fun HttpErrorBody.isVerificationError() = this.error == "verification_process_closed"

    private fun HttpErrorBody.isInvalidCredentialError() = this.error == "invalid_credential"

    companion object {
        private val ERRORS = listOf(
            "authorization_request_object_not_found",
            "authorization_request_missing_error_param",
            "invalid_presentation_definition",
            "invalid_request",
        )
    }
}

private fun Throwable.toFetchPresentationRequestError(): FetchPresentationRequestError = when (this) {
    is IOException -> PresentationRequestError.NetworkError
    else -> PresentationRequestError.Unexpected(this)
}

private fun Throwable.toSubmitAnyCredentialPresentationError(): SubmitAnyCredentialPresentationError =
    when (this) {
        is IOException -> PresentationRequestError.NetworkError
        else -> PresentationRequestError.Unexpected(this)
    }

private fun Throwable.toSubmitPresentationErrorError(): SubmitPresentationErrorError = when (this) {
    is IOException -> PresentationRequestError.NetworkError
    else -> PresentationRequestError.Unexpected(this)
}

private fun JsonParsingError.toSubmitPresentationError(): SubmitAnyCredentialPresentationError = when (this) {
    is JsonError.Unexpected -> PresentationRequestError.Unexpected(throwable)
}
