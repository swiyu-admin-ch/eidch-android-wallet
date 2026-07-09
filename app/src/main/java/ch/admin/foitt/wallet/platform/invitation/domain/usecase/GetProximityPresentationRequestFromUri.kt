package ch.admin.foitt.wallet.platform.invitation.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.openid4vc.domain.model.proximity.ProximityPresentationRequest
import ch.admin.foitt.wallet.platform.invitation.domain.model.GetProximityPresentationRequestError
import com.github.michaelbull.result.Result
import java.net.URI

fun interface GetProximityPresentationRequestFromUri {
    @CheckResult
    suspend operator fun invoke(uri: URI): Result<ProximityPresentationRequest, GetProximityPresentationRequestError>
}
