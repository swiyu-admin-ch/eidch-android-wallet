package ch.admin.foitt.wallet.platform.credentialPresentation.domain.model

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointer
import kotlinx.serialization.Serializable

@Serializable
data class PresentationRequestField(
    val path: ClaimsPathPointer,
    val value: String,
)
