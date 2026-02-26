package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class GuardianConsentResultState {
    QUEUEING_LEGAL_CONSENT_PENDING,
    QUEUEING_LEGAL_CONSENT_OK,
    AV_READY_LEGAL_CONSENT_PENDING,
    AV_EXPIRED_LEGAL_CONSENT_PENDING,
}
