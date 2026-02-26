package ch.admin.foitt.wallet.platform.activityList.domain.model

data class ActivityDisplayData(
    val id: Long,
    val activityType: ActivityType,
    val date: String,
    val nonComplianceData: String?,
    val localizedActorName: String,
    val actorImageData: ByteArray? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ActivityDisplayData

        if (id != other.id) return false
        if (activityType != other.activityType) return false
        if (date != other.date) return false
        if (nonComplianceData != other.nonComplianceData) return false
        if (localizedActorName != other.localizedActorName) return false
        if (!actorImageData.contentEquals(other.actorImageData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + activityType.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + nonComplianceData.hashCode()
        result = 31 * result + localizedActorName.hashCode()
        result = 31 * result + (actorImageData?.contentHashCode() ?: 0)
        return result
    }
}
