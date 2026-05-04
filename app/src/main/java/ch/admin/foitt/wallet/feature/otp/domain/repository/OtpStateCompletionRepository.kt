package ch.admin.foitt.wallet.feature.otp.domain.repository

interface OtpStateCompletionRepository {
    suspend fun setOtpFlowWasDone(isCompleted: Boolean)
    suspend fun getOtpFlowWasDone(): Boolean
}
