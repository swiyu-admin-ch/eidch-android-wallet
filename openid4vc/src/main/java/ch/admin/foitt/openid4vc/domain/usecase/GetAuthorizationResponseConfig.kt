package ch.admin.foitt.openid4vc.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponse
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseConfig
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.GetAuthorizationResponseConfigError
import com.github.michaelbull.result.Result

interface GetAuthorizationResponseConfig {
    operator fun invoke(
        authorizationRequest: AuthorizationRequest,
        authorizationResponse: AuthorizationResponse,
        usePayloadEncryption: Boolean,
    ): Result<AuthorizationResponseConfig, GetAuthorizationResponseConfigError>
}
