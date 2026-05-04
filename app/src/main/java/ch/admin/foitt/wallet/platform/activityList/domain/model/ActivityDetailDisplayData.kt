package ch.admin.foitt.wallet.platform.activityList.domain.model

import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.ActorComplianceState
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus

data class ActivityDetailDisplayData(
    val activityId: Long,
    val activityType: ActivityType,
    val date: String,
    val localizedActorName: String,
    val actorTrustStatus: TrustStatus,
    val vcSchemaTrustStatus: VcSchemaTrustStatus,
    val actorComplianceState: ActorComplianceState,
    val localizedNonComplianceReason: String? = null,
    val actorImageData: ByteArray? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ActivityDetailDisplayData

        if (activityId != other.activityId) return false
        if (activityType != other.activityType) return false
        if (date != other.date) return false
        if (localizedActorName != other.localizedActorName) return false
        if (actorTrustStatus != other.actorTrustStatus) return false
        if (vcSchemaTrustStatus != other.vcSchemaTrustStatus) return false
        if (actorComplianceState != other.actorComplianceState) return false
        if (localizedNonComplianceReason != other.localizedNonComplianceReason) return false
        if (!actorImageData.contentEquals(other.actorImageData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = activityId.hashCode()
        result = 31 * result + activityType.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + localizedActorName.hashCode()
        result = 31 * result + actorTrustStatus.hashCode()
        result = 31 * result + vcSchemaTrustStatus.hashCode()
        result = 31 * result + actorComplianceState.hashCode()
        result = 31 * result + (localizedNonComplianceReason?.hashCode() ?: 0)
        result = 31 * result + (actorImageData?.contentHashCode() ?: 0)
        return result
    }
}
