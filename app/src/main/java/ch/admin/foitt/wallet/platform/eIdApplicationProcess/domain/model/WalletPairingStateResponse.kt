package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WalletPairingStateResponse(
    @SerialName("state")
    val state: WalletPairingState
)
