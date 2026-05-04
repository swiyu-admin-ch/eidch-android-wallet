package ch.admin.foitt.wallet.platform.activityList.domain.model

data class ActivityActorDisplayData(
    val activityId: Long,
    val localizedActorName: String,
    val actorImageData: ByteArray? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ActivityActorDisplayData

        if (activityId != other.activityId) return false
        if (localizedActorName != other.localizedActorName) return false
        if (!actorImageData.contentEquals(other.actorImageData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = activityId.hashCode()
        result = 31 * result + localizedActorName.hashCode()
        result = 31 * result + (actorImageData?.contentHashCode() ?: 0)
        return result
    }
}
