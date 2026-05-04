package ch.admin.foitt.openid4vc.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.CreateAnyVerifiablePresentationError
import com.github.michaelbull.result.Result

internal fun interface CreateAnyVerifiablePresentation {
    suspend operator fun invoke(
        anyCredential: AnyCredential,
        requestedFields: List<String>,
        authorizationRequest: AuthorizationRequest,
    ): Result<String, CreateAnyVerifiablePresentationError>
}
