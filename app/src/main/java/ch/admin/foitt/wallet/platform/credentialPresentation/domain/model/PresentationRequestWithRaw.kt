package ch.admin.foitt.wallet.platform.credentialPresentation.domain.model

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import kotlinx.serialization.Serializable

@Serializable
data class PresentationRequestWithRaw(
    val presentationRequest: PresentationRequest,
    val rawPresentationRequest: String,
)
