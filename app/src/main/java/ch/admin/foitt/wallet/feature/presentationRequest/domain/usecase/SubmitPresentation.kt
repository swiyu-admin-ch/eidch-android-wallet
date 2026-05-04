package ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.SubmitPresentationError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import com.github.michaelbull.result.Result

fun interface SubmitPresentation {

    suspend operator fun invoke(
        authorizationRequest: AuthorizationRequest,
        compatibleCredential: CompatibleCredential,
    ): Result<Unit, SubmitPresentationError>
}
