package ch.admin.foitt.wallet.platform.activityList.domain.usecase

import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData

interface SavePresentationAcceptedActivity {
    suspend operator fun invoke(
        credentialId: Long,
        actorDisplayData: ActorDisplayData,
        verifierFallbackName: String,
        claimIds: List<Long>,
        nonComplianceData: String?,
    )
}
