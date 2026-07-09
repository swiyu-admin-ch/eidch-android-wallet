package ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.implementation

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import ch.admin.foitt.wallet.platform.utils.AppVersion
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.AppVersionInfo
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.EnforcementType
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.VersionEnforcement
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.VersionEnforcement.Display
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.repository.VersionEnforcementRepository
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.FetchAppVersionInfo
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.GetAppVersion
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.GetDeviceModel
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.GetOSVersion
import com.github.michaelbull.result.mapBoth
import java.time.LocalDate
import javax.inject.Inject

internal class FetchAppVersionInfoImpl @Inject constructor(
    private val versionEnforcementRepository: VersionEnforcementRepository,
    private val getAppVersion: GetAppVersion,
    private val getLocalizedDisplay: GetLocalizedDisplay,
    private val getDeviceModel: GetDeviceModel,
    private val getOsVersion: GetOSVersion,
) : FetchAppVersionInfo {

    val currentVersion by lazy { getAppVersion() }
    val osVersion by lazy { getOsVersion() }

    @CheckResult
    override suspend fun invoke(): AppVersionInfo =
        versionEnforcementRepository.fetchVersionEnforcement()
            .mapBoth(
                success = ::checkEnforcement,
                failure = { AppVersionInfo.Unknown }
            )

    private fun AppVersionInfo.takeIfValid(block: () -> AppVersionInfo): AppVersionInfo {
        return if (this is AppVersionInfo.Valid) block() else this
    }

    private fun getCorrectMessage(enforcement: VersionEnforcement): List<Display> {
        val matchingVersion = enforcement.versions.find { it.version == currentVersion }
        return matchingVersion?.message ?: enforcement.displays
    }

    private fun checkEnforcement(enforcement: VersionEnforcement?): AppVersionInfo {
        if (enforcement == null) return AppVersionInfo.Valid

        return checkEnforcementForMinimumOsVersion(enforcement)
            .takeIfValid { checkEnforcementForBlackList(enforcement) }
            .takeIfValid { checkPreferredVersion(enforcement) }
    }

    private fun checkEnforcementForMinimumOsVersion(enforcement: VersionEnforcement): AppVersionInfo {
        val currentOs = AppVersion(osVersion)

        return if (currentOs < enforcement.minOSVersion) {
            AppVersionInfo.Blocked(null, null, enforcement.storeUrl, EnforcementType.OS_UPDATE)
        } else {
            AppVersionInfo.Valid
        }
    }

    private fun checkEnforcementForBlackList(enforcement: VersionEnforcement): AppVersionInfo {
        return if (enforcement.defaultBlacklist.contains(getDeviceModel())) {
            AppVersionInfo.Blocked(null, null, enforcement.storeUrl, EnforcementType.DEVICE_BLACKLIST)
        } else {
            AppVersionInfo.Valid
        }
    }

    private fun checkPreferredVersion(enforcement: VersionEnforcement): AppVersionInfo {
        return versionForForcedUpgrade(enforcement)
            .takeIfValid { versionForOptionalUpgradeForUngaranteedVersion(enforcement) }
            .takeIfValid { versionForOptionalUpgradeForExpiredVersion(enforcement) }
            .takeIfValid { versionForOptionalUpdateSuggested(enforcement) }
    }

    private fun versionForForcedUpgrade(enforcement: VersionEnforcement): AppVersionInfo {
        /**
         * If the application is running version X, and (for the given platform)
         *
         *    there is at least one versions entry
         *    with update_type "forced",
         *    and with a higher version >X
         *
         * then the application must not start and the user shall be told to update the app.
         */
        return if (enforcement.versions.any { it.updateType == "forced" && it.version > currentVersion }) {
            val display = getLocalizedDisplay(getCorrectMessage(enforcement))
            AppVersionInfo.Blocked(display?.title, display?.text, enforcement.storeUrl, EnforcementType.APP_BLOCKED)
        } else {
            AppVersionInfo.Valid
        }
    }

    private fun versionForOptionalUpgradeForUngaranteedVersion(enforcement: VersionEnforcement): AppVersionInfo {
        /**
         * If the application is running version X, and (for the given platform)
         *
         *     there is a versions entry
         *     with update_type "optional",
         *     and with matching or higher version >=X
         *     and with support_guaranteed_until in the past,
         *
         * then the application must not start and the user shall be told to update the app.
         */
        return if (enforcement.versions.any {
                it.updateType == "optional" &&
                    it.version >= currentVersion &&
                    it.supportUntil.isBefore(LocalDate.now())
            }
        ) {
            val display = getLocalizedDisplay(getCorrectMessage(enforcement))
            AppVersionInfo.Blocked(display?.title, display?.text, enforcement.storeUrl, EnforcementType.APP_BLOCKED)
        } else {
            AppVersionInfo.Valid
        }
    }

    private fun versionForOptionalUpgradeForExpiredVersion(enforcement: VersionEnforcement): AppVersionInfo {
        /** If the application is running version X, and (for the given platform)
         *
         *    there is a versions entry
         *    with update_type "optional",
         *    and with matching or higher version >=X
         *    and with no support_guaranteed_until
         *    and (release_date + default_support_lifetime_days) in the past,
         *
         * then the application must not start and the user shall be told to update the app.
         */
        val lifetimeDays = enforcement.lifetime.toLong()

        val hasExpiredLifetime = enforcement.versions.any {
            it.updateType == "optional" &&
                it.version >= currentVersion &&
                it.releaseDate.plusDays(lifetimeDays).isBefore(LocalDate.now())
        }

        return if (hasExpiredLifetime) {
            val display = getLocalizedDisplay(getCorrectMessage(enforcement))
            AppVersionInfo.Blocked(display?.title, display?.text, enforcement.storeUrl, EnforcementType.APP_BLOCKED)
        } else {
            AppVersionInfo.Valid
        }
    }

    private fun versionForOptionalUpdateSuggested(enforcement: VersionEnforcement): AppVersionInfo {
        /**
         *  Find the latest optional version that is newer than current and still valid
         */
        val today = LocalDate.now()
        val lifetimeDays = enforcement.lifetime.toLong()

        val suggestedVersion = enforcement.versions.find {
            it.updateType == "optional" &&
                it.version > currentVersion &&
                it.supportUntil.isAfter(today) &&
                it.releaseDate.plusDays(lifetimeDays).isAfter(today)
        }

        return if (suggestedVersion != null) {
            val display = getLocalizedDisplay(getCorrectMessage(enforcement))
            AppVersionInfo.Blocked(display?.title, display?.text, enforcement.storeUrl, EnforcementType.UPDATE_SUGGESTED)
        } else {
            AppVersionInfo.Valid
        }
    }
}
