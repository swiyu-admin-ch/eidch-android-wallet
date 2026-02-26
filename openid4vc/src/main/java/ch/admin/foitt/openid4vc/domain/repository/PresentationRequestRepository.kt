package ch.admin.foitt.openid4vc.domain.repository

import androidx.annotation.CheckResult
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.FetchPresentationRequestError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestErrorBody
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestType
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.SubmitAnyCredentialPresentationError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.SubmitPresentationErrorError
import com.github.michaelbull.result.Result
import java.net.URL

interface PresentationRequestRepository {
    @CheckResult
    suspend fun fetchPresentationRequest(url: URL): Result<String, FetchPresentationRequestError>

    suspend fun submitPresentation(
        url: URL,
        presentationRequestType: PresentationRequestType,
    ): Result<Unit, SubmitAnyCredentialPresentationError>

    suspend fun submitPresentationError(
        url: String,
        body: PresentationRequestErrorBody
    ): Result<Unit, SubmitPresentationErrorError>
}
