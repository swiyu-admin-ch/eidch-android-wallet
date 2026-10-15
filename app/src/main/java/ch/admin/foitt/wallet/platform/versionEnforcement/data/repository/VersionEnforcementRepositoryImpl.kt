package ch.admin.foitt.wallet.platform.versionEnforcement.data.repository

import ch.admin.foitt.openid4vc.di.OpenId4VcModule.Companion.NAMED_DEFAULT_HTTP_CLIENT
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.utils.CacheManager
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.FetchVersionEnforcementError
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.VersionEnforcementError
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.VersionEnforcementRaw
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.repository.VersionEnforcementRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class VersionEnforcementRepositoryImpl @Inject constructor(
    @param:Named(NAMED_DEFAULT_HTTP_CLIENT) private val httpClient: HttpClient,
    private val environmentSetupRepo: EnvironmentSetupRepository,
    private val cache: CacheManager<VersionEnforcementRaw>,
) : VersionEnforcementRepository {
    override suspend fun fetchVersionEnforcement(): Result<VersionEnforcementRaw?, FetchVersionEnforcementError> =
        runSuspendCatching {
            if (!environmentSetupRepo.isVersionEnforcementEnabled) {
                return@runSuspendCatching null
            }
            cache.get {
                val response = httpClient.get(environmentSetupRepo.appVersionEnforcementUrl) {
                    contentType(ContentType.Application.Json)
                }
                VersionEnforcementRaw(
                    body = response.bodyAsText(),
                    detachedJws = response.headers["detached-jws"],
                )
            }
        }.mapError { throwable ->
            throwable.toFetchVersionEnforcementInfoError("VersionEnforcementRepository error")
        }
}

private fun Throwable.toFetchVersionEnforcementInfoError(message: String): FetchVersionEnforcementError {
    Timber.e(t = this, message = message)
    return VersionEnforcementError.Unexpected(this)
}
