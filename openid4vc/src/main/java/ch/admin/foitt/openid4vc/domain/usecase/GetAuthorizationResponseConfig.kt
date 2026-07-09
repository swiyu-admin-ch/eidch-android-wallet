package ch.admin.foitt.openid4vc.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointer
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseConfig
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.GetAuthorizationResponseConfigError
import com.github.michaelbull.result.Result

interface GetAuthorizationResponseConfig {
    suspend operator fun invoke(
        anyCredential: AnyCredential,
        presentationPaths: List<ClaimsPathPointer>,
        authorizationRequest: AuthorizationRequest,
        usePayloadEncryption: Boolean,
        dcqlQueryId: String?,
    ): Result<AuthorizationResponseConfig, GetAuthorizationResponseConfigError>
}
