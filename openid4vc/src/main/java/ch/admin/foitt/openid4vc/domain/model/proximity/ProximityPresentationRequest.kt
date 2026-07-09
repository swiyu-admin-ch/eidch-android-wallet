package ch.admin.foitt.openid4vc.domain.model.proximity

import ch.admin.foitt.openid4vc.domain.model.Invitation
import kotlinx.serialization.Serializable

@Serializable
data class ProximityPresentationRequest(
    val rawQrData: String,
) : Invitation
