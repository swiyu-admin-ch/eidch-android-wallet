package ch.admin.foitt.wallet.platform.nonCompliance.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class NonComplianceVerifierReportReason(override val type: String) : NonComplianceReportReason {
    EXCESSIVE_DATA_REQUEST("ExcessiveDataRequest"),
    VERIFICATION_NOT_REGISTERED("VerificationNotRegistered"),
    FALSE_IDENTITY_VERIFIER("FalseIdentityVerifier"),
    OTHER("Other")
}
