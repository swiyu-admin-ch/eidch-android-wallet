package ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.RequestObject
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestWithRaw
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ValidatePresentationRequestError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.VerificationProcessType
import com.github.michaelbull.result.Result

fun interface ValidatePresentationRequest {
    suspend operator fun invoke(
        verificationProcessType: VerificationProcessType,
        requestObject: RequestObject
    ): Result<PresentationRequestWithRaw, ValidatePresentationRequestError>
}
