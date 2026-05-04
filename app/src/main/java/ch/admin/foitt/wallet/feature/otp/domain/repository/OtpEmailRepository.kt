package ch.admin.foitt.wallet.feature.otp.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface OtpEmailRepository {
    val email: StateFlow<String?>
    fun setEmail(email: String)
}
