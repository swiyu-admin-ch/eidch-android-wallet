package ch.admin.foitt.wallet.platform.messageEvents.data.repository

import ch.admin.foitt.wallet.platform.messageEvents.domain.model.NonComplianceEvent
import ch.admin.foitt.wallet.platform.messageEvents.domain.repository.NonComplianceEventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class NonComplianceEventRepositoryImpl @Inject constructor() : NonComplianceEventRepository {
    private val _event = MutableStateFlow(NonComplianceEvent.NONE)
    override val event = _event.asStateFlow()

    override fun setEvent(event: NonComplianceEvent) {
        _event.value = event
    }

    override fun resetEvent() {
        _event.value = NonComplianceEvent.NONE
    }
}
