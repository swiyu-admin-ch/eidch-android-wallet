package ch.admin.foitt.wallet.platform.invitation.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestWithRaw
import ch.admin.foitt.wallet.platform.invitation.domain.model.GetPresentationRequestError
import com.github.michaelbull.result.Result
import java.net.URI

fun interface GetPresentationRequestFromUri {
    @CheckResult
    suspend operator fun invoke(uri: URI): Result<PresentationRequestWithRaw, GetPresentationRequestError>
}
