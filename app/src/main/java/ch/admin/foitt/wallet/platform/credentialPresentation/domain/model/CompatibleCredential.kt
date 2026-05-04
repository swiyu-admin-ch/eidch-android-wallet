package ch.admin.foitt.wallet.platform.credentialPresentation.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CompatibleCredential(
    val credentialId: Long,
    val requestedFields: List<PresentationRequestField>,
    val dcqlQueryId: String? = null,
)
