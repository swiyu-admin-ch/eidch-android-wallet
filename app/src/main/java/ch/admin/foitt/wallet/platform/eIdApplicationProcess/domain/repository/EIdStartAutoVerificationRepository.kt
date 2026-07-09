package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.AutoVerificationResponse
import kotlinx.coroutines.flow.StateFlow

interface EIdStartAutoVerificationRepository {
    val autoVerificationResponse: StateFlow<AutoVerificationResponse?>
    fun setAutoVerificationResponse(response: AutoVerificationResponse)
}
