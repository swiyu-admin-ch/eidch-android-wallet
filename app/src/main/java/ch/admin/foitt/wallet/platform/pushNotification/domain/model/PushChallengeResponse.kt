package ch.admin.foitt.wallet.platform.pushNotification.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PushChallengeResponse(
    @SerialName("challenge")
    val nonce: String
)
