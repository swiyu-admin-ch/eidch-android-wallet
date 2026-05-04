package ch.admin.foitt.wallet.feature.otp.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface OtpToastRepository {
    val showToast: StateFlow<Boolean>
    fun setShowToast(showToast: Boolean)
}
