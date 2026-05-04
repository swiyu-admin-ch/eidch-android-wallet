package ch.admin.foitt.wallet.feature.otp.data.repository

import ch.admin.foitt.wallet.feature.otp.domain.repository.OtpToastRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class OtpToastRepositoryImpl @Inject constructor() : OtpToastRepository {
    private val _showToast = MutableStateFlow(false)
    override val showToast = _showToast.asStateFlow()
    override fun setShowToast(showToast: Boolean) {
        _showToast.value = showToast
    }
}
