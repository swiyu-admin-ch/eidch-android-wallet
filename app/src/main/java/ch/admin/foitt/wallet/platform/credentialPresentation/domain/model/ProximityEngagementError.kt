package ch.admin.foitt.wallet.platform.credentialPresentation.domain.model

import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.NetworkError
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.Unexpected
import ch.admin.foitt.wallet.platform.invitation.domain.model.ProcessInvitationError

sealed class ProximityEngagementError {
    data class Unexpected(val throwable: Throwable?) : ProximityEngagementError()
    data object NoCompatibleCredential : ProximityEngagementError()
    data object Disconnected : ProximityEngagementError()
    data object UnexpectedTermination : ProximityEngagementError()
}

internal fun ProximityEngagementError.toProcessInvitationError(): ProcessInvitationError = when (this) {
    is ProximityEngagementError.Disconnected -> NetworkError
    is ProximityEngagementError.Unexpected,
    ProximityEngagementError.UnexpectedTermination -> Unexpected
    ProximityEngagementError.NoCompatibleCredential -> InvitationError.NoCompatibleCredential(null)
}

internal fun ProximitySubmissionError.toProximityEngagementError(): ProximityEngagementError = when (this) {
    is ProximitySubmissionError.Failed -> ProximityEngagementError.Unexpected(cause)
    is ProximitySubmissionError.UnexpectedTermination -> ProximityEngagementError.UnexpectedTermination
}
