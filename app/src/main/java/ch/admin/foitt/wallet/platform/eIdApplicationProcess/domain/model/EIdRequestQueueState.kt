package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model

enum class EIdRequestQueueState {
    IN_QUEUING,
    READY_FOR_ONLINE_SESSION,
    IN_TARGET_WALLET_PAIRING,
    IN_AUTO_VERIFICATION,
    WAITING_FOR_VERIFICATION_APPROVAL,
    READY_FOR_FINAL_ENTITLEMENT_CHECK,
    IN_ISSUANCE,
    REFUSED,
    CANCELLED,
    TIMEOUT,
    CLOSED
}
