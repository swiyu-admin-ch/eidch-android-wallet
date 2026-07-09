package ch.admin.foitt.wallet.feature.home.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.home.domain.usecase.EIdRequestsPriorityOrdering
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.SIdRequestDisplayData
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.priority
import javax.inject.Inject

class EIdRequestsPriorityOrderingImpl @Inject constructor() : EIdRequestsPriorityOrdering {
    override suspend fun invoke(eIdRequests: List<SIdRequestDisplayData>): List<SIdRequestDisplayData> {
        return eIdRequests.sortedWith(
            compareBy<SIdRequestDisplayData> {
                it.status.priority
            }.thenByDescending { it.createdAt }
        )
    }
}
