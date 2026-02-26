package ch.admin.foitt.openid4vc.data

import ch.admin.foitt.openid4vc.di.ExternalOpenId4VcModule.Companion.NAMED_DEFAULT_HTTP_CLIENT
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSchemaRepositoryError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.toVcSchemaRepositoryError
import ch.admin.foitt.openid4vc.domain.repository.VcSchemaRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import java.net.URL
import javax.inject.Inject
import javax.inject.Named

internal class VcSchemaRepositoryImpl @Inject constructor(
    @param:Named(NAMED_DEFAULT_HTTP_CLIENT) private val httpClient: HttpClient,
) : VcSchemaRepository {

    override suspend fun fetchVcSchema(url: URL): Result<String, VcSchemaRepositoryError> = runSuspendCatching<String> {
        httpClient.get(url) {
            headers {
                append("Accept", HEADERS.joinToString(","))
            }
        }.body()
    }.mapError { throwable ->
        throwable.toVcSchemaRepositoryError(message = "Fetch vc schema error")
    }

    private companion object {
        const val APPLICATION_JSON = "application/json"
        const val APPLICATION_SCHEMA_JSON = "application/schema+json"
        const val APPLICATION_SCHEMA_INSTANCE_JSON = "application/schema-instance+json"
        val HEADERS = listOf(APPLICATION_JSON, APPLICATION_SCHEMA_JSON, APPLICATION_SCHEMA_INSTANCE_JSON)
    }
}
