package ch.admin.foitt.wallet.platform.activityList.presentation.model

import androidx.compose.ui.graphics.painter.Painter
import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityDetailDisplayData
import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityType
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.ActorComplianceState
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus

data class ActivityDetailUiState(
    val id: Long,
    val activityType: ActivityType,
    val date: String,
    val localizedActorName: String,
    val actorImage: Painter? = null,
    val actorTrust: TrustStatus,
    val vcSchemaTrust: VcSchemaTrustStatus,
    val actorCompliance: ActorComplianceState,
    val nonComplianceReason: String? = null,
)

fun ActivityDetailDisplayData.toActivityDetailUiState(actorImage: Painter?) = ActivityDetailUiState(
    id = activityId,
    activityType = activityType,
    date = date,
    localizedActorName = localizedActorName,
    actorImage = actorImage,
    actorTrust = actorTrustStatus,
    vcSchemaTrust = vcSchemaTrustStatus,
    actorCompliance = actorComplianceState,
    nonComplianceReason = localizedNonComplianceReason
)
