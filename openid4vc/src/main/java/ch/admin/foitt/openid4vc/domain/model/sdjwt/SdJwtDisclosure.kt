package ch.admin.foitt.openid4vc.domain.model.sdjwt

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointer

data class SdJwtDisclosure(
    val paths: List<ClaimsPathPointer>,
    val disclosure: String,
)
