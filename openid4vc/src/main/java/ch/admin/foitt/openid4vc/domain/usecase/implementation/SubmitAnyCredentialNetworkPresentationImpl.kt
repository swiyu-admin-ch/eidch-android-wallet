package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseConfig
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.SubmitAnyCredentialPresentationError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.toSubmitAnyCredentialPresentationError
import ch.admin.foitt.openid4vc.domain.repository.PresentationRequestRepository
import ch.admin.foitt.openid4vc.domain.usecase.SubmitAnyCredentialNetworkPresentation
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import java.net.URL
import javax.inject.Inject

internal class SubmitAnyCredentialNetworkPresentationImpl @Inject constructor(
    private val presentationRequestRepository: PresentationRequestRepository,
) : SubmitAnyCredentialNetworkPresentation {
    override suspend fun invoke(
        authorizationRequest: AuthorizationRequest,
        authorizationResponseConfig: AuthorizationResponseConfig
    ): Result<Unit, SubmitAnyCredentialPresentationError> = coroutineBinding {
        val responseURL = runSuspendCatching { URL(authorizationRequest.responseUri) }
            .mapError { throwable -> throwable.toSubmitAnyCredentialPresentationError("presentationRequest.responseUri error") }
            .bind()

        presentationRequestRepository.submitPresentation(
            url = responseURL,
            authorizationResponseConfig = authorizationResponseConfig,
        ).bind()
    }
}
