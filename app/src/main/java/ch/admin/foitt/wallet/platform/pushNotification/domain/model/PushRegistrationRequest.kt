package ch.admin.foitt.wallet.platform.pushNotification.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PushRegistrationRequest(
    @SerialName("push_device_token")
    val deviceToken: String,
    @SerialName("platform_os")
    val platform: String,
)
