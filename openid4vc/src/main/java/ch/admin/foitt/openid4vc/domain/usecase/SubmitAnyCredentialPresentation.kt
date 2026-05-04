package ch.admin.foitt.openid4vc.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.SubmitAnyCredentialPresentationError
import com.github.michaelbull.result.Result

fun interface SubmitAnyCredentialPresentation {
    suspend operator fun invoke(
        anyCredential: AnyCredential,
        requestedFields: List<String>,
        authorizationRequest: AuthorizationRequest,
        usePayloadEncryption: Boolean,
        dcqlQueryId: String?,
    ): Result<Unit, SubmitAnyCredentialPresentationError>
}
