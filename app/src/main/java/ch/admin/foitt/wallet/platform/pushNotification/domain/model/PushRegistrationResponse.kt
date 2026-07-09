package ch.admin.foitt.wallet.platform.pushNotification.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PushRegistrationResponse(
    @SerialName("pushId")
    val pushId: String
)
