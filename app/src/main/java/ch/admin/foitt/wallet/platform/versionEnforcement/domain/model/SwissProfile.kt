package ch.admin.foitt.wallet.platform.versionEnforcement.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SwissProfile(
    @SerialName("profile")
    val type: ProfileType,
    @SerialName("status")
    val status: Status,
    @SerialName("version")
    val version: String,
) {
    enum class ProfileType {
        @SerialName("swiss-profile-anchor")
        ANCHOR,

        @SerialName("swiss-profile-issuance")
        ISSUANCE,

        @SerialName("swiss-profile-portability")
        PORTABILITY,

        @SerialName("swiss-profile-proximity")
        PROXIMITY,

        @SerialName("swiss-profile-trust")
        TRUST,

        @SerialName("swiss-profile-vc")
        VERIFIABLE_CREDENTIAL,

        @SerialName("swiss-profile-verification")
        VERIFICATION,
    }

    enum class Status {
        @SerialName("current")
        CURRENT,

        @SerialName("deprecated")
        DEPRECATED,

        @SerialName("sunset")
        SUNSET,

        @SerialName("eol")
        END_OF_LIFE;

        val freshness: Int
            get() = when (this) {
                CURRENT -> 3
                DEPRECATED -> 2
                SUNSET -> 1
                END_OF_LIFE -> 0
            }

        val refreshAttemptIntervalDays: Long
            get() = when (this) {
                CURRENT -> 7
                DEPRECATED -> 5
                SUNSET -> 2
                END_OF_LIFE -> 1
            }
    }
}
