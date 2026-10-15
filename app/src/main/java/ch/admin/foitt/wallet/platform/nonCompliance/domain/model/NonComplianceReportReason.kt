package ch.admin.foitt.wallet.platform.nonCompliance.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface NonComplianceReportReason {
    val type: String
}
