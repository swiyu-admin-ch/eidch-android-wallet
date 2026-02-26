package ch.admin.foitt.openid4vc.domain.model.credentialoffer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface CredentialResponse {
    @Serializable
    data class VerifiableCredential(
        @SerialName("credentials")
        val credentials: List<Credential>,
        @SerialName("notification_id")
        val notificationId: String? = null,
    ) : CredentialResponse

    @Serializable
    data class Credential(
        @SerialName("credential")
        val credential: String,
    )

    @Serializable
    data class DeferredCredential(
        @SerialName("transaction_id")
        val transactionId: String,
        @SerialName("interval")
        val interval: Int,
    ) : CredentialResponse
}
