package ch.admin.foitt.wallet.platform.oca.util

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointer
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointerComponent

fun createClaimsPathPointer(vararg parts: Any?): ClaimsPathPointer =
    parts.map { part ->
        when (part) {
            null -> ClaimsPathPointerComponent.Null
            is String -> ClaimsPathPointerComponent.String(part)
            is Int -> ClaimsPathPointerComponent.Index(part)
            else -> error("invalid data type")
        }
    }
