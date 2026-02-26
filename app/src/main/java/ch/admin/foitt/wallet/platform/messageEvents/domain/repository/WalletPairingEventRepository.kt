package ch.admin.foitt.wallet.platform.messageEvents.domain.repository

import ch.admin.foitt.wallet.platform.messageEvents.domain.model.WalletPairingEvent
import kotlinx.coroutines.flow.StateFlow

interface WalletPairingEventRepository {
    val event: StateFlow<WalletPairingEvent>

    fun setEvent(event: WalletPairingEvent)
    fun resetEvent()
}
