package ch.admin.foitt.wallet.platform.activityList.domain.repository

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityRepositoryError
import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityType
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import com.github.michaelbull.result.Result

interface ActivityRepository {
    suspend fun saveActivity(
        credentialId: Long,
        activityType: ActivityType,
        actorDisplayData: ActorDisplayData,
        actorFallbackName: String,
        claimIds: List<Long>? = null,
        nonComplianceData: String?,
    ): Result<Long?, ActivityRepositoryError>
}
