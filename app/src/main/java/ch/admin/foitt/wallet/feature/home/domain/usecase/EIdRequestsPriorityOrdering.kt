package ch.admin.foitt.wallet.feature.home.domain.usecase

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.SIdRequestDisplayData

interface EIdRequestsPriorityOrdering {
    suspend operator fun invoke(eIdRequests: List<SIdRequestDisplayData>): List<SIdRequestDisplayData>
}
