package ch.admin.foitt.wallet.platform.trustRegistry.data

import ch.admin.foitt.openid4vc.di.ExternalOpenId4VcModule.Companion.NAMED_DEFAULT_HTTP_CLIENT
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementRepositoryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.toTrustStatementRepositoryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.repository.TrustStatementRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import java.net.URL
import javax.inject.Inject
import javax.inject.Named

class TrustStatementRepositoryImpl @Inject constructor(
    @param:Named(NAMED_DEFAULT_HTTP_CLIENT) private val httpClient: HttpClient,
) : TrustStatementRepository {
    override suspend fun fetchTrustStatements(url: URL): Result<List<String>, TrustStatementRepositoryError> =
        runSuspendCatching<List<String>> {
            httpClient.get(url).body()
        }.mapError { throwable ->
            throwable.toTrustStatementRepositoryError("TrustStatementRepository error")
        }
}
