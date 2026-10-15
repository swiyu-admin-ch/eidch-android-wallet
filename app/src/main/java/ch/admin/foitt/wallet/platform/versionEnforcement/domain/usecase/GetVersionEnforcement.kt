package ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase

import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.FetchVersionEnforcementError
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.VersionEnforcement
import com.github.michaelbull.result.Result

interface GetVersionEnforcement {
    suspend operator fun invoke(): Result<VersionEnforcement?, FetchVersionEnforcementError>
}
