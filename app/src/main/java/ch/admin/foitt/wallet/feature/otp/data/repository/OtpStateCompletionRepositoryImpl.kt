package ch.admin.foitt.wallet.feature.otp.data.repository

import androidx.security.crypto.EncryptedSharedPreferences
import ch.admin.foitt.wallet.feature.otp.domain.repository.OtpStateCompletionRepository
import javax.inject.Inject

class OtpStateCompletionRepositoryImpl @Inject constructor(
    private val sharedPreferences: EncryptedSharedPreferences,
) : OtpStateCompletionRepository {

    private val prefKey = "otp_completed"

    override suspend fun getOtpFlowWasDone() = sharedPreferences.getBoolean(prefKey, false)

    override suspend fun setOtpFlowWasDone(isCompleted: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean(prefKey, isCompleted)
            apply()
        }
    }
}
