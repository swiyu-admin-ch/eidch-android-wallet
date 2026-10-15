package ch.admin.foitt.wallet.platform.nonCompliance.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class NonComplianceIssuerReportReason(override val type: String) : NonComplianceReportReason {
    DUBIOUS_CREDENTIAL_ISSUANCE("DubiousCredentialIssuance"),
    SENSITIVE_DATA_NOT_DECLARED("SensitiveDataNotDeclared"),
    FALSE_IDENTITY("FalseIdentity"),
    ILLEGAL_CONTENT_OR_PURPOSE("IllegalContentOrPurpose"),
    OTHER("Other")
}
