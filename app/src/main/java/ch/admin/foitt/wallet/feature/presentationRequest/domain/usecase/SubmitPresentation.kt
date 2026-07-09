package ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase

import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.SubmitPresentationError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestWithRaw
import com.github.michaelbull.result.Result

fun interface SubmitPresentation {

    suspend operator fun invoke(
        presentationRequestWithRaw: PresentationRequestWithRaw,
        compatibleCredential: CompatibleCredential,
    ): Result<Unit, SubmitPresentationError>
}
