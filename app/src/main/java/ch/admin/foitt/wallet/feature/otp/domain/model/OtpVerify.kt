package ch.admin.foitt.wallet.feature.otp.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OtpVerify(
    @SerialName("email")
    val email: String,

    @SerialName("code")
    val code: String
)
