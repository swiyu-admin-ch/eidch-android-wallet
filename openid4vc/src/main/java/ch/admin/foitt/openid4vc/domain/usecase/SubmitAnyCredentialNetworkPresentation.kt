package ch.admin.foitt.openid4vc.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseConfig
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.SubmitAnyCredentialPresentationError
import com.github.michaelbull.result.Result

fun interface SubmitAnyCredentialNetworkPresentation {
    suspend operator fun invoke(
        authorizationRequest: AuthorizationRequest,
        authorizationResponseConfig: AuthorizationResponseConfig
    ): Result<Unit, SubmitAnyCredentialPresentationError>
}
