package ch.admin.foitt.wallet.platform.activityList.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ActivityType {
    ISSUANCE, PRESENTATION_ACCEPTED, PRESENTATION_DECLINED
}
