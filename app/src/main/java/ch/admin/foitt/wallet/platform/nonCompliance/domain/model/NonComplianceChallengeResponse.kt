package ch.admin.foitt.wallet.platform.nonCompliance.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NonComplianceChallengeResponse(
    @SerialName("challenge")
    val challenge: String,
)
