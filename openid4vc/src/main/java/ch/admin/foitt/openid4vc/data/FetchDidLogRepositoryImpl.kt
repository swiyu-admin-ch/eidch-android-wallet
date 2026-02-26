package ch.admin.foitt.openid4vc.data

import ch.admin.foitt.openid4vc.di.ExternalOpenId4VcModule.Companion.NAMED_DEFAULT_HTTP_CLIENT
import ch.admin.foitt.openid4vc.domain.model.ResolveDidError
import ch.admin.foitt.openid4vc.domain.repository.FetchDidLogRepository
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import timber.log.Timber
import java.net.URL
import javax.inject.Inject
import javax.inject.Named

class FetchDidLogRepositoryImpl @Inject constructor(
    @param:Named(NAMED_DEFAULT_HTTP_CLIENT) private val httpClient: HttpClient,
) : FetchDidLogRepository {
    override suspend fun fetchDidLog(url: URL) = runSuspendCatching<String> {
        httpClient.get(url) {
            accept(ContentType("application", "jsonl+json"))
        }.bodyAsText()
    }.mapError { throwable ->
        Timber.d(t = throwable, message = "Did resolver network error")
        ResolveDidError.NetworkError
    }
}
