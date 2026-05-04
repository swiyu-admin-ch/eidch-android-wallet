package ch.admin.foitt.wallet.feature.otp.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OtpRequest(
    @SerialName("email")
    val email: String
)
