package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.GetTrustDomainFromDidError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.GetTrustUrlFromDidError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementType
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.toGetTrustUrlFromDidError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.GetTrustDomainFromDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.GetTrustUrlFromDid
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import io.ktor.utils.io.charsets.name
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject

internal class GetTrustUrlFromDidImpl @Inject constructor(
    private val getTrustDomainFromDid: GetTrustDomainFromDid
) : GetTrustUrlFromDid {
    override fun invoke(
        trustStatementType: TrustStatementType,
        actorDid: String,
        vcSchemaId: String?,
    ): Result<URL, GetTrustUrlFromDidError> = binding {
        val trustDomain = getTrustDomainFromDid(actorDid = actorDid)
            .mapError(GetTrustDomainFromDidError::toGetTrustUrlFromDidError)
            .bind()

        buildTrustUrl(
            trustStatementType = trustStatementType,
            actorDid = actorDid,
            trustDomain = trustDomain,
            vcSchemaId = vcSchemaId,
        ).bind()
    }

    private fun buildTrustUrl(
        trustStatementType: TrustStatementType,
        actorDid: String,
        trustDomain: String,
        vcSchemaId: String?,
    ): Result<URL, GetTrustUrlFromDidError> = runSuspendCatching {
        val trustPathBase = "$TRUST_SCHEME$trustDomain$TRUST_PATH"
        val urlString = when (trustStatementType) {
            TrustStatementType.IDENTITY -> {
                val didUrlEncoded = URLEncoder.encode(actorDid, Charsets.UTF_8.name)
                "$trustPathBase$TRUST_PATH_IDENTITY$didUrlEncoded"
            }

            TrustStatementType.ISSUANCE -> {
                val vcSchemaIdUrlEncoded = URLEncoder.encode(vcSchemaId, Charsets.UTF_8.name)
                "$trustPathBase$TRUST_PATH_ISSUANCE?vcSchemaId=$vcSchemaIdUrlEncoded"
            }

            TrustStatementType.VERIFICATION -> {
                val vcSchemaIdUrlEncoded = URLEncoder.encode(vcSchemaId, Charsets.UTF_8.name)
                "$trustPathBase$TRUST_PATH_VERIFICATION?vcSchemaId=$vcSchemaIdUrlEncoded"
            }
        }

        URL(urlString)
    }.mapError { throwable ->
        throwable.toGetTrustUrlFromDidError(message = "Failed to build trust URL")
    }

    private companion object {
        const val TRUST_SCHEME = "https://"
        const val TRUST_PATH = "/api/v1/truststatements/"
        const val TRUST_PATH_IDENTITY = "identity/"
        const val TRUST_PATH_ISSUANCE = "issuance/"
        const val TRUST_PATH_VERIFICATION = "verification/"
    }
}
