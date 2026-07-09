package ch.admin.foitt.wallet.platform.versionEnforcement.domain.model

import ch.admin.foitt.wallet.platform.database.domain.model.LocalizedDisplay
import ch.admin.foitt.wallet.platform.utils.AppVersion
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class VersionEnforcement(
    @SerialName("app_id")
    val appId: String,
    @SerialName("default_message")
    val displays: List<Display>,
    @SerialName("default_support_lifetime_days")
    val lifetime: Int,
    @SerialName("default_blacklist")
    val defaultBlacklist: List<String> = emptyList(),
    @Serializable(with = AppVersionSerializer::class)
    @SerialName("minimum_os_version")
    val minOSVersion: AppVersion,
    @SerialName("platform")
    val platform: Platform,
    @SerialName("store_url")
    val storeUrl: String,
    @SerialName("versions")
    val versions: List<Versions>,
) {
    enum class Platform {
        @SerialName("android")
        ANDROID,
        OTHER,
    }

    @Serializable
    data class Display(
        @SerialName("title")
        val title: String,
        @SerialName("body")
        val text: String,
        @SerialName("locale")
        override val locale: String,
    ) : LocalizedDisplay

    @Serializable
    data class Versions(
        @SerialName("message")
        val message: List<Display>,
        @Serializable(with = LocalDateSerializer::class)
        @SerialName("release_date")
        val releaseDate: LocalDate,
        @Serializable(with = LocalDateSerializer::class)
        @SerialName("support_guaranteed_until")
        val supportUntil: LocalDate,
        @SerialName("update_type")
        val updateType: String,
        @Serializable(with = AppVersionSerializer::class)
        @SerialName("version")
        val version: AppVersion
    )
}
