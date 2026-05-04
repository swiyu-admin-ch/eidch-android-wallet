package ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestWithRaw
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ProcessPresentationRequestError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ProcessPresentationRequestResult
import com.github.michaelbull.result.Result

fun interface ProcessPresentationRequest {
    @CheckResult
    suspend operator fun invoke(
        presentationRequestWithRaw: PresentationRequestWithRaw,
    ): Result<ProcessPresentationRequestResult, ProcessPresentationRequestError>
}
