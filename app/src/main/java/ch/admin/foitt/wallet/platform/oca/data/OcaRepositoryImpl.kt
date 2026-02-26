package ch.admin.foitt.wallet.platform.oca.data

import ch.admin.foitt.openid4vc.di.ExternalOpenId4VcModule.Companion.NAMED_DEFAULT_HTTP_CLIENT
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaRepositoryError
import ch.admin.foitt.wallet.platform.oca.domain.model.toOcaRepositoryError
import ch.admin.foitt.wallet.platform.oca.domain.repository.OcaRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.http.ContentType
import java.net.URL
import javax.inject.Inject
import javax.inject.Named

class OcaRepositoryImpl @Inject constructor(
    @param:Named(NAMED_DEFAULT_HTTP_CLIENT) private val httpClient: HttpClient,
) : OcaRepository {
    override suspend fun fetchOcaBundleByUrl(url: URL): Result<String, OcaRepositoryError> = runSuspendCatching<String> {
        httpClient.get(url) {
            accept(ContentType.Application.Json)
        }.body()
    }.mapError { throwable ->
        throwable.toOcaRepositoryError("fetchOcaBundle error")
    }
}
