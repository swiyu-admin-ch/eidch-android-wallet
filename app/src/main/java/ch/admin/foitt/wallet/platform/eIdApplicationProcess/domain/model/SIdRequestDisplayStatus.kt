package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model

import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestQueueState.CANCELLED
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestQueueState.IN_ISSUANCE
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestQueueState.IN_QUEUING
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
    IN_AGENT_REVIEW,
    IN_ISSUANCE,
    REFUSED,
    UNKNOWN,
    OTHER,
}

fun StateResponse.toSIdRequestDisplayStatus(): SIdRequestDisplayStatus =
    RequestStatusInput(state, toLegalRepresentativeConsent()).toSIdRequestDisplayStatus()

fun EIdRequestState.toSIdRequestDisplayStatus(): SIdRequestDisplayStatus =
    RequestStatusInput(state, legalRepresentativeConsent).toSIdRequestDisplayStatus()

private fun RequestStatusInput.toSIdRequestDisplayStatus(): SIdRequestDisplayStatus = when (this.queueState) {
    READY_FOR_ONLINE_SESSION -> handleReadyState(this.legalConsent)
    IN_QUEUING -> handleQueueingState(this.legalConsent)
    in listOf(TIMEOUT, CANCELLED) -> handleExpiredState(this.legalConsent)
    WAITING_FOR_VERIFICATION_APPROVAL -> SIdRequestDisplayStatus.IN_AGENT_REVIEW
    REFUSED -> SIdRequestDisplayStatus.REFUSED
    // We currently do not try to differentiate if other devices are paired
    IN_ISSUANCE -> SIdRequestDisplayStatus.IN_ISSUANCE
    else -> SIdRequestDisplayStatus.OTHER
}

private fun handleReadyState(legalConsent: LegalRepresentativeConsent?): SIdRequestDisplayStatus = when (legalConsent) {
    NOT_REQUIRED -> SIdRequestDisplayStatus.AV_READY
    VERIFIED -> SIdRequestDisplayStatus.AV_READY_LEGAL_CONSENT_OK
    NOT_VERIFIED -> SIdRequestDisplayStatus.AV_READY_LEGAL_CONSENT_PENDING
    else -> SIdRequestDisplayStatus.OTHER
}

private fun handleQueueingState(legalConsent: LegalRepresentativeConsent?): SIdRequestDisplayStatus = when (legalConsent) {
    NOT_REQUIRED -> SIdRequestDisplayStatus.QUEUEING
    VERIFIED -> SIdRequestDisplayStatus.QUEUEING_LEGAL_CONSENT_OK
    NOT_VERIFIED -> SIdRequestDisplayStatus.QUEUEING_LEGAL_CONSENT_PENDING
    else -> SIdRequestDisplayStatus.OTHER
}

private fun handleExpiredState(legalConsent: LegalRepresentativeConsent?): SIdRequestDisplayStatus = when (legalConsent) {
    NOT_REQUIRED -> SIdRequestDisplayStatus.AV_EXPIRED
    VERIFIED -> SIdRequestDisplayStatus.AV_EXPIRED_LEGAL_CONSENT_OK
    NOT_VERIFIED -> SIdRequestDisplayStatus.AV_EXPIRED_LEGAL_CONSENT_PENDING
    else -> SIdRequestDisplayStatus.OTHER
}
