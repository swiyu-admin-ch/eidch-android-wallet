package ch.admin.foitt.wallet.platform.proximity.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents an age option that can be selected during the verification flow.
 */
@Serializable
enum class VerificationAgeOption(val claimName: String, val age: Int) {
    SIXTEEN_PLUS("age_over_16", 16),
    EIGHTEEN_PLUS("age_over_18", 18),
    SIXTYFIVE_PLUS("age_over_65", 65)
}
