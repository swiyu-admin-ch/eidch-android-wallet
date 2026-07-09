package ch.admin.foitt.wallet.platform.proximity.domain.model

sealed interface ProximityEngagementUpdate {
    data class QrCode(val qrCode: String) : ProximityEngagementUpdate
    data class Request(val request: String) : ProximityEngagementUpdate
}
