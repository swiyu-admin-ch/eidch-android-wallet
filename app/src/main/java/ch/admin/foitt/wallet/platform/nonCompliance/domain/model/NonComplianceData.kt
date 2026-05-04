package ch.admin.foitt.wallet.platform.nonCompliance.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class NonComplianceData(
    val state: ActorComplianceState,
    val reasonDisplays: List<NonComplianceReasonDisplay>?,
)
