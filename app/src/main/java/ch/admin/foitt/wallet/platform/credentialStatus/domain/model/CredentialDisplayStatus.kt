package ch.admin.foitt.wallet.platform.credentialStatus.domain.model

import java.time.Instant

/**
 * Credential status resulting from the union of :
 *  - a status-list status
 *  - a credential validity
 *
 * It is used for display purpose
 */
sealed interface CredentialDisplayStatus {
    data object Valid : CredentialDisplayStatus
    data object Revoked : CredentialDisplayStatus
    data object Suspended : CredentialDisplayStatus
    data object Unsupported : CredentialDisplayStatus
    data object Unknown : CredentialDisplayStatus
    data class BusinessExpired(val expiredAt: Instant?) : CredentialDisplayStatus
    data class Expired(val expiredAt: Instant) : CredentialDisplayStatus
    data class NotYetValid(val validFrom: Instant) : CredentialDisplayStatus
}
