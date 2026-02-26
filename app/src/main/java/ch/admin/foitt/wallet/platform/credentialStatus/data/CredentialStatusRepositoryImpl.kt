package ch.admin.foitt.wallet.platform.credentialStatus.data

import ch.admin.foitt.openid4vc.di.ExternalOpenId4VcModule.Companion.NAMED_DEFAULT_HTTP_CLIENT
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialStatusError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.FetchStatusFromTokenStatusListError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.repository.CredentialStatusRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

class CredentialStatusRepositoryImpl @Inject constructor(
    @param:Named(NAMED_DEFAULT_HTTP_CLIENT) private val httpClient: HttpClient,
) : CredentialStatusRepository {
    override suspend fun fetchTokenStatusListJwt(url: String): Result<String, FetchStatusFromTokenStatusListError> =
        runSuspendCatching<String> {
            httpClient.get(url) {
                header(HttpHeaders.Accept, "application/statuslist+jwt")
            }.body()
        }.mapError { throwable ->
            throwable.toFetchStatusFromStatusListError("fetchTokenStatusListJwt error")
        }
}

private fun Throwable.toFetchStatusFromStatusListError(message: String): FetchStatusFromTokenStatusListError {
    Timber.e(t = this, message = message)
    return when (this) {
        is IOException -> CredentialStatusError.NetworkError
        else -> CredentialStatusError.Unexpected(this)
    }
}
