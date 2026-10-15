package ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase

import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.SwissProfile

fun interface GetSwissProfileStatus {
    suspend operator fun invoke(profileVersion: String?): SwissProfile.Status?
}
