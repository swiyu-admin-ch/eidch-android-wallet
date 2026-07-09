package ch.admin.foitt.wallet.platform.invitation.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.proximity.ProximityPresentationRequest
import ch.admin.foitt.wallet.platform.invitation.domain.model.GetProximityPresentationRequestError
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.GetProximityPresentationRequestFromUri
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import java.net.URI
import javax.inject.Inject

internal class GetProximityPresentationRequestFromUriImpl @Inject constructor() : GetProximityPresentationRequestFromUri {
    override suspend fun invoke(uri: URI): Result<ProximityPresentationRequest, GetProximityPresentationRequestError> = coroutineBinding {
        ProximityPresentationRequest(uri.rawSchemeSpecificPart)
    }
}
