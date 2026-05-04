package ch.admin.foitt.wallet.platform.oca.domain.util

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointer
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointerComponent

fun naiveJsonPathToClaimsPathPointer(jsonPath: String): ClaimsPathPointer {
    val claimName = jsonPath.replace("$.", "")
    return listOf(ClaimsPathPointerComponent.String(claimName))
}
