package ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.SwissProfile
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.GetSwissProfileStatus
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.GetVersionEnforcement
import com.github.michaelbull.result.get
import javax.inject.Inject

class GetSwissProfileStatusImpl @Inject constructor(
    private val getVersionEnforcement: GetVersionEnforcement,
) : GetSwissProfileStatus {
    override suspend fun invoke(profileVersion: String?): SwissProfile.Status? {
        val vcProfiles = getVersionEnforcement().get()
            ?.swissProfileLifecycle?.vc
            ?.filter { it.type == SwissProfile.ProfileType.VERIFIABLE_CREDENTIAL }
            .orEmpty()

        if (vcProfiles.isEmpty()) {
            return SwissProfile.Status.CURRENT
        }

        val version = profileVersion
            ?.takeIf { it.startsWith(VC_PROFILE_PREFIX) }
            ?.removePrefix(VC_PROFILE_PREFIX)
        return vcProfiles.firstOrNull { it.version == version }?.status
    }

    private companion object {
        const val VC_PROFILE_PREFIX = "swiss-profile-vc:"
    }
}
