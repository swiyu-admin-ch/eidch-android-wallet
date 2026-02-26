package ch.admin.foitt.wallet.platform.messageEvents.data.repository

import ch.admin.foitt.wallet.platform.messageEvents.domain.model.WalletPairingEvent
import ch.admin.foitt.wallet.platform.messageEvents.domain.repository.WalletPairingEventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class WalletPairingEventRepositoryImpl @Inject constructor() : WalletPairingEventRepository {
    private val _event = MutableStateFlow(WalletPairingEvent.NONE)
    override val event = _event.asStateFlow()

    override fun setEvent(event: WalletPairingEvent) {
        _event.value = event
    }

    override fun resetEvent() {
        _event.value = WalletPairingEvent.NONE
    }
}
