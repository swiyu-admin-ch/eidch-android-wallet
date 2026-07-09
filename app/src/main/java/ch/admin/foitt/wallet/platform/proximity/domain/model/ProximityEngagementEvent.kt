package ch.admin.foitt.wallet.platform.proximity.domain.model

import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ProcessPresentationRequestResult

sealed interface ProximityEngagementEvent {
    data class QrCode(val qrCode: String) : ProximityEngagementEvent
    data class Request(val processPresentationRequestResult: ProcessPresentationRequestResult) : ProximityEngagementEvent
}
