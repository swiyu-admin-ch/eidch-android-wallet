package ch.admin.foitt.wallet.openid4vc

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.FetchPresentationRequestError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseErrorBody
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseConfig
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.SubmitAnyCredentialPresentationError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.SubmitPresentationErrorError
import ch.admin.foitt.openid4vc.domain.repository.PresentationRequestRepository
import ch.admin.foitt.wallet.feature.presentationRequest.mock.PresentationRequestMocks.MOCK_AUTHORIZATION_REQUEST
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import java.net.URL
import javax.inject.Inject

class FakePresentationRequestRepositoryImpl @Inject constructor(
) : PresentationRequestRepository {
    override suspend fun fetchPresentationRequest(url: URL): Result<String, FetchPresentationRequestError> {
        return Ok(MOCK_AUTHORIZATION_REQUEST)
    }

    override suspend fun submitPresentation(
        url: URL,
        authorizationResponseConfig: AuthorizationResponseConfig
    ): Result<Unit, SubmitAnyCredentialPresentationError> {
        return Ok(Unit)
    }

    override suspend fun submitPresentationError(
        url: String,
        body: AuthorizationResponseErrorBody
    ): Result<Unit, SubmitPresentationErrorError> {
        return Ok(Unit)
    }
}
