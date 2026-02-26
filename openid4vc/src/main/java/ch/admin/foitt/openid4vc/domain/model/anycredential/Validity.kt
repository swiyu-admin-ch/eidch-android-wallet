package ch.admin.foitt.openid4vc.domain.model.anycredential

import java.time.Instant

sealed interface Validity {
    data class NotYetValid(val validFrom: Instant) : Validity
    data object Valid : Validity
    data class Expired(val expiredAt: Instant) : Validity
}
