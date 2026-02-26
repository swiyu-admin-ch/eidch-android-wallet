
package ch.admin.foitt.wallet.platform.nonCompliance.data.repository

import ch.admin.foitt.openid4vc.di.ExternalOpenId4VcModule.Companion.NAMED_GZIP_HTTP_CLIENT
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceRepositoryError
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceResponse
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.toNonComplianceRepositoryError
import ch.admin.foitt.wallet.platform.nonCompliance.domain.repository.NonComplianceTrustRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import java.net.URL
import javax.inject.Inject
import javax.inject.Named

class NonComplianceTrustRepositoryImpl @Inject constructor(
    @param:Named(NAMED_GZIP_HTTP_CLIENT) private val httpClient: HttpClient,
) : NonComplianceTrustRepository {
    override suspend fun fetchNonComplianceData(
        trustRegistryDomain: String
    ): Result<NonComplianceResponse, NonComplianceRepositoryError> = runSuspendCatching {
        val url = URL("https://$trustRegistryDomain/api/v1/non-compliant-actors")
        httpClient.get(url).body<NonComplianceResponse>()
    }.mapError { throwable ->
        throwable.toNonComplianceRepositoryError("error when trying to fetch non compliance data")
    }
}
