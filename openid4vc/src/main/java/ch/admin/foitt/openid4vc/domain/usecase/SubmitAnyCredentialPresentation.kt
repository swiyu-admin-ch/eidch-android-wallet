package ch.admin.foitt.openid4vc.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.SubmitAnyCredentialPresentationError
import com.github.michaelbull.result.Result

fun interface SubmitAnyCredentialPresentation {
    suspend operator fun invoke(
        anyCredential: AnyCredential,
        requestedFields: List<String>,
        presentationRequest: PresentationRequest,
        usePayloadEncryption: Boolean,
    ): Result<Unit, SubmitAnyCredentialPresentationError>
}
