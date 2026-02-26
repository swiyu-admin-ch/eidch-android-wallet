package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StateResponse(
    @SerialName("state")
    val state: EIdRequestQueueState,
    @SerialName("queueInformation")
    val queueInformation: QueueInformation?,
    @SerialName("legalRepresentant")
    val legalRepresentant: LegalRepresentant?,
    @SerialName("onlineSessionStartTimeout")
    val onlineSessionStartTimeout: String?,
    @SerialName("targetWallets")
    val targetWallets: TargetWallets?,
)

@Serializable
data class QueueInformation(
    @SerialName("positionInQueue")
    val positionInQueue: Long?,
    @SerialName("totalInQueue")
    val totalInQueue: Long?,
    @SerialName("expectedOnlineSessionStart")
    val expectedOnlineSessionStart: String?
)

@Serializable
data class LegalRepresentant(
    @SerialName("verified")
    val verified: Boolean,
    @SerialName("verificationLink")
    val verificationLink: String
)

@Serializable
data class TargetWallets(
    @SerialName("limitReached")
    val limitReached: Boolean,
    @SerialName("pairedWallets")
    val pairedWallets: List<PairedWallets>
)

@Serializable
data class PairedWallets(
    @SerialName("walletPairingId")
    val walletPairingId: String,
    @SerialName("timestampPairing")
    val timestampPairing: String,
    @SerialName("timestampCollecting")
    val timestampCollecting: String?,
)
