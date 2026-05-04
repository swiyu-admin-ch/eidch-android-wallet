package ch.admin.foitt.wallet.platform.credentialPresentation.domain.model

import ch.admin.foitt.openid4vc.domain.model.Invitation
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import kotlinx.serialization.Serializable

@Serializable
data class PresentationRequestWithRaw(
    val authorizationRequest: AuthorizationRequest,
    val rawPresentationRequest: String,
) : Invitation
