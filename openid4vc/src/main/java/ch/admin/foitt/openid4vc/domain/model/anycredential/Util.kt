package ch.admin.foitt.openid4vc.domain.model.anycredential

import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.get
import com.github.michaelbull.result.recoverCatching
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

fun getValidity(validFrom: Long?, validUntil: Long?, businessExpiredValidity: Instant? = null): Validity {
    val leeway = 15L

    val validFromInstant = validFrom?.let { Instant.ofEpochSecond(it) }
    val validFromWithLeeway = validFromInstant?.minusSeconds(leeway)
    val validUntilInstant = validUntil?.let { Instant.ofEpochSecond(it) }
    val validUntilWithLeeway = validUntilInstant?.plusSeconds(leeway)
    val now = Instant.now()

    return when {
        validFromWithLeeway != null && now.isBefore(validFromWithLeeway) -> Validity.NotYetValid(validFromInstant)
        validUntilWithLeeway != null && now.isAfter(validUntilWithLeeway) -> Validity.Expired(validUntilInstant)
        businessExpiredValidity != null && now.isAfter(businessExpiredValidity) -> Validity.BusinessExpired(businessExpiredValidity)
        else -> Validity.Valid
    }
}

fun String.toBusinessExpiryInstant(): Instant? = toInstant() ?: runSuspendCatching {
    OffsetDateTime.parse(this).toLocalDate().atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC)
}.recoverCatching {
    LocalDate.parse(this).atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC)
}.get()

internal fun String.toInstant(): Instant? = runSuspendCatching {
    Instant.ofEpochSecond(this.toLong())
}.recoverCatching {
    Instant.parse(this)
}.get()
