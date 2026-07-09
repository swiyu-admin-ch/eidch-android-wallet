package ch.admin.foitt.wallet.platform.pushNotification.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PushUpdateTokenRequest(
    @SerialName("push_ids")
    val pushIds: List<String>,
    @SerialName("push_device_token")
    val pushDeviceToken: String
)
