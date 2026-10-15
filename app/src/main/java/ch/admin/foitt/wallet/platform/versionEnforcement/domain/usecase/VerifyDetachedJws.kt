package ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.FetchVersionEnforcementError
import com.github.michaelbull.result.Result

fun interface VerifyDetachedJws {
    @CheckResult
    suspend operator fun invoke(jws: String, body: String): Result<Unit, FetchVersionEnforcementError>
}
