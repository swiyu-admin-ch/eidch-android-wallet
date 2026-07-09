package ch.admin.foitt.wallet.platform.proximity.domain.model

import kotlinx.serialization.Serializable

/**
 * Type of verification to be performed
 */
@Serializable
sealed class VerificationType {
    /**
     * Age verification - verify that the user meets a minimum age requirement
     */
    @Serializable
    data class Age(var options: List<VerificationAgeOption>) : VerificationType()

    /**
     * Personal data verification - verify specific personal data attributes
     */
    @Serializable
    object PersonalData : VerificationType()
}
