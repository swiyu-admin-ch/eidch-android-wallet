package ch.admin.foitt.wallet.platform.nonCompliance.domain.model

data class NonComplianceTextInputConstraints(
    val minLength: Int = 20,
    val maxLength: Int = 500,
)
