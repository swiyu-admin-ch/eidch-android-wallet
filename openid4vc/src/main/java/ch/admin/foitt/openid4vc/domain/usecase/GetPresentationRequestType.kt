package ch.admin.foitt.openid4vc.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.GetPresentationRequestTypeError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestBody
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestType
import com.github.michaelbull.result.Result

interface GetPresentationRequestType {
    operator fun invoke(
        presentationRequest: PresentationRequest,
        presentationRequestBody: PresentationRequestBody,
        usePayloadEncryption: Boolean,
    ): Result<PresentationRequestType, GetPresentationRequestTypeError>
}
