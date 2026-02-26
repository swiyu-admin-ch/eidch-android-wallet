package ch.admin.foitt.openid4vc.domain.model.anycredential

import java.time.Instant

fun getValidity(validFrom: Long?, validUntil: Long?): Validity {
    val leeway = 15L

    val validFromInstant = validFrom?.let { Instant.ofEpochSecond(it) }
    val validFromWithLeeway = validFromInstant?.minusSeconds(leeway)
    val validUntilInstant = validUntil?.let { Instant.ofEpochSecond(it) }
    val validUntilWithLeeway = validUntilInstant?.plusSeconds(leeway)
    val now = Instant.now()

    return when {
        validFromWithLeeway != null && now.isBefore(validFromWithLeeway) -> Validity.NotYetValid(validFromInstant)
        validUntilWithLeeway != null && now.isAfter(validUntilWithLeeway) -> Validity.Expired(validUntilInstant)
        else -> Validity.Valid
    }
}
