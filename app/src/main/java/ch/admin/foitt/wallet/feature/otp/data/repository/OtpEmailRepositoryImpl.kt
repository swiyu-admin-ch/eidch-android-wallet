package ch.admin.foitt.wallet.feature.otp.data.repository

import ch.admin.foitt.wallet.feature.otp.domain.repository.OtpEmailRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class OtpEmailRepositoryImpl @Inject constructor() : OtpEmailRepository {
    private val _email = MutableStateFlow<String?>(null)
    override val email = _email.asStateFlow()
    override fun setEmail(email: String) {
        _email.value = email
    }
}
