package ch.admin.foitt.wallet.platform.nonCompliance.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class NonComplianceReportReason(val type: String) {
    EXCESSIVE_DATA_REQUEST("ExcessiveDataRequest")
}
