package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EIdPeerPushIdRequest(
    @SerialName("pushId")
    val pushId: String
)
