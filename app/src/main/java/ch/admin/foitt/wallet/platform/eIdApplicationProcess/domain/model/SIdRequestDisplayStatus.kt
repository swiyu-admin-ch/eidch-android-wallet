package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model

import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestQueueState.CANCELLED
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestQueueState.CLOSED
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestQueueState.IN_AUTO_VERIFICATION
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestQueueState.IN_ISSUANCE
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestQueueState.IN_QUEUING
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestQueueState.IN_TARGET_WALLET_PAIRING
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestQueueState.READY_FOR_FINAL_ENTITLEMENT_CHECK
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestQueueState.READY_FOR_ONLINE_SESSION
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestQueueState.REFUSED
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestQueueState.TIMEOUT
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestQueueState.WAITING_FOR_VERIFICATION_APPROVAL
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.LegalRepresentativeConsent.NOT_REQUIRED
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.LegalRepresentativeConsent.NOT_VERIFIED
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.LegalRepresentativeConsent.VERIFIED

enum class SIdRequestDisplayStatus {
    AV_READY,
    AV_READY_LEGAL_CONSENT_OK,
    AV_READY_LEGAL_CONSENT_PENDING,
    QUEUEING,
    QUEUEING_LEGAL_CONSENT_OK,
    QUEUEING_LEGAL_CONSENT_PENDING,
    AV_EXPIRED,
    AV_EXPIRED_LEGAL_CONSENT_OK,
    AV_EXPIRED_LEGAL_CONSENT_PENDING,
    IN_AUTO_VERIFICATION,
    IN_TARGET_WALLET_PAIRING,
    IN_AGENT_REVIEW,
    READY_FOR_FINAL_ENTITLEMENT_CHECK,
    IN_ISSUANCE,
    CANCELLED,
    REFUSED,
    CLOSED,
    UNKNOWN,
}

fun StateResponse.toSIdRequestDisplayStatus(): SIdRequestDisplayStatus =
    getSIdRequestDisplayStatus(state, toLegalRepresentativeConsent())

fun EIdRequestState.toSIdRequestDisplayStatus(): SIdRequestDisplayStatus =
    getSIdRequestDisplayStatus(state, legalRepresentativeConsent)

private fun getSIdRequestDisplayStatus(
    queueState: EIdRequestQueueState,
    legalConsent: LegalRepresentativeConsent,
): SIdRequestDisplayStatus = when (queueState) {
    READY_FOR_ONLINE_SESSION -> handleReadyState(legalConsent)
    IN_QUEUING -> handleQueueingState(legalConsent)
    WAITING_FOR_VERIFICATION_APPROVAL -> SIdRequestDisplayStatus.IN_AGENT_REVIEW
    REFUSED -> SIdRequestDisplayStatus.REFUSED
    // We currently do not try to differentiate if other devices are paired
    IN_ISSUANCE -> SIdRequestDisplayStatus.IN_ISSUANCE
    IN_TARGET_WALLET_PAIRING -> SIdRequestDisplayStatus.IN_TARGET_WALLET_PAIRING
    IN_AUTO_VERIFICATION -> SIdRequestDisplayStatus.IN_AUTO_VERIFICATION
    READY_FOR_FINAL_ENTITLEMENT_CHECK -> SIdRequestDisplayStatus.READY_FOR_FINAL_ENTITLEMENT_CHECK
    CANCELLED -> SIdRequestDisplayStatus.CANCELLED
    TIMEOUT -> handleExpiredState(legalConsent)
    CLOSED -> SIdRequestDisplayStatus.CLOSED
}

private fun handleReadyState(legalConsent: LegalRepresentativeConsent): SIdRequestDisplayStatus = when (legalConsent) {
    NOT_REQUIRED -> SIdRequestDisplayStatus.AV_READY
    VERIFIED -> SIdRequestDisplayStatus.AV_READY_LEGAL_CONSENT_OK
    NOT_VERIFIED -> SIdRequestDisplayStatus.AV_READY_LEGAL_CONSENT_PENDING
}

private fun handleQueueingState(legalConsent: LegalRepresentativeConsent): SIdRequestDisplayStatus = when (legalConsent) {
    NOT_REQUIRED -> SIdRequestDisplayStatus.QUEUEING
    VERIFIED -> SIdRequestDisplayStatus.QUEUEING_LEGAL_CONSENT_OK
    NOT_VERIFIED -> SIdRequestDisplayStatus.QUEUEING_LEGAL_CONSENT_PENDING
}

private fun handleExpiredState(legalConsent: LegalRepresentativeConsent): SIdRequestDisplayStatus = when (legalConsent) {
    NOT_REQUIRED -> SIdRequestDisplayStatus.AV_EXPIRED
    VERIFIED -> SIdRequestDisplayStatus.AV_EXPIRED_LEGAL_CONSENT_OK
    NOT_VERIFIED -> SIdRequestDisplayStatus.AV_EXPIRED_LEGAL_CONSENT_PENDING
}
