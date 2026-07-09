package ch.admin.foitt.wallet.platform.versionEnforcement.domain.repository

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.FetchVersionEnforcementError
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.VersionEnforcement
import com.github.michaelbull.result.Result

interface VersionEnforcementRepository {
    @CheckResult
    suspend fun fetchVersionEnforcement(): Result<VersionEnforcement?, FetchVersionEnforcementError>
}
