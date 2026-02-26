package ch.admin.foitt.wallet.platform.messageEvents.domain.repository

import ch.admin.foitt.wallet.platform.messageEvents.domain.model.NonComplianceEvent
import kotlinx.coroutines.flow.StateFlow

interface NonComplianceEventRepository {
    val event: StateFlow<NonComplianceEvent>

    fun setEvent(event: NonComplianceEvent)
    fun resetEvent()
}
