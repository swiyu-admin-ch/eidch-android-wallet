package ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase

import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.FetchAndCacheIssuerDisplayDataError
import com.github.michaelbull.result.Result

interface FetchAndCacheIssuerDisplayData {
    suspend operator fun invoke(
        credentialId: Long,
    ): Result<Unit, FetchAndCacheIssuerDisplayDataError>
}
