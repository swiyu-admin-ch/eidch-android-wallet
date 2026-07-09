package ch.admin.foitt.wallet.platform.trustRegistry.domain.model

enum class TrustStatus {
    TRUSTED,
    NOT_TRUSTED,
    TRUSTED_PROXIMITY_VERIFIER,
    NOT_TRUSTED_PROXIMITY_VERIFIER,
    EXTERNAL,
    UNKNOWN,
}
