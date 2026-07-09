package ch.admin.foitt.wallet.platform.versionEnforcement.domain.model

sealed interface AppVersionInfo {
    data class Blocked(
        val title: String?,
        val text: String?,
        val playStoreUrl: String? = null,
        val type: EnforcementType = EnforcementType.APP_BLOCKED
    ) : AppVersionInfo
    data object Valid : AppVersionInfo
    data object Unknown : AppVersionInfo
}
