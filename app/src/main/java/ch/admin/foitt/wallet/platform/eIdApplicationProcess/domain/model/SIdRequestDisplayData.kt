package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model

data class SIdRequestDisplayData(
    val caseId: String,
    val status: SIdRequestDisplayStatus,
    val firstName: String,
    val lastName: String,
    val onlineSessionStartOpenAt: String? = null,
    val onlineSessionStartTimeoutAt: String? = null,
    val createdAt: Long,
)
