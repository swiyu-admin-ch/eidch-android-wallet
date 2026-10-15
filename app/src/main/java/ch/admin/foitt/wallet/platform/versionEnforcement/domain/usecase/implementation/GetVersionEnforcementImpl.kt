package ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.utils.JsonError
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import ch.admin.foitt.wallet.platform.utils.SafeJson
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.FetchVersionEnforcementError
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.VersionEnforcement
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.VersionEnforcementError
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.repository.VersionEnforcementRepository
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.GetVersionEnforcement
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.VerifyDetachedJws
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import timber.log.Timber
import javax.inject.Inject

class GetVersionEnforcementImpl @Inject constructor(
    private val versionEnforcementRepository: VersionEnforcementRepository,
    private val verifyDetachedJws: VerifyDetachedJws,
    private val safeJson: SafeJson,
) : GetVersionEnforcement {
    override suspend fun invoke(): Result<VersionEnforcement?, FetchVersionEnforcementError> =
        coroutineBinding {
            val versionEnforcementRaw = versionEnforcementRepository.fetchVersionEnforcement().bind() ?: return@coroutineBinding null

            val body = versionEnforcementRaw.body
            val detachedJws = versionEnforcementRaw.detachedJws ?: Err(VersionEnforcementError.SignatureValidationError).bind()

            verifyDetachedJws(detachedJws, body).bind()

            safeJson.safeDecodeStringTo<VersionEnforcement>(body)
                .mapError { it.toFetchVersionEnforcementInfoError("VersionEnforcementRepository parse error") }
                .bind()
        }
}

private fun JsonParsingError.toFetchVersionEnforcementInfoError(message: String): FetchVersionEnforcementError = when (this) {
    is JsonError.Unexpected -> throwable.toFetchVersionEnforcementInfoError(message)
}

private fun Throwable.toFetchVersionEnforcementInfoError(message: String): FetchVersionEnforcementError {
    Timber.e(t = this, message = message)
    return VersionEnforcementError.Unexpected(this)
}
