package ch.admin.foitt.wallet.platform.activityList.domain.usecase

import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData

interface SaveIssuanceActivity {
    suspend operator fun invoke(
        credentialId: Long,
        actorDisplayData: ActorDisplayData,
        issuerFallbackName: String,
    ): Long?
}
