package ch.admin.foitt.wallet.platform.credential.domain.usecase

import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustCheckResult

fun interface FetchTrustForIssuance {
    suspend operator fun invoke(
        issuerDid: String,
        vcSchemaId: String,
    ): TrustCheckResult
}
