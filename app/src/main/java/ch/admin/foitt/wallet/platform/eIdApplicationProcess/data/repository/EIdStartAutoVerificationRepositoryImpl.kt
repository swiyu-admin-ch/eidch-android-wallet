package ch.admin.foitt.wallet.platform.eIdApplicationProcess.data.repository

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.AutoVerificationResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdStartAutoVerificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class EIdStartAutoVerificationRepositoryImpl @Inject constructor() : EIdStartAutoVerificationRepository {
    private val _autoVerificationResponse = MutableStateFlow<AutoVerificationResponse?>(null)
    override val autoVerificationResponse = _autoVerificationResponse.asStateFlow()
    override fun setAutoVerificationResponse(response: AutoVerificationResponse) {
        _autoVerificationResponse.value = response
    }
}
