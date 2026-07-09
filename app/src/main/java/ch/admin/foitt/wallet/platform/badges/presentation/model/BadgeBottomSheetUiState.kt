package ch.admin.foitt.wallet.platform.badges.presentation.model

import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.badges.domain.model.BadgeType

sealed interface BadgeBottomSheetUiState {

    data class TrustVerified(
        val actorName: String,
        val onMoreInformation: () -> Unit,
    ) : BadgeBottomSheetUiState

    data class TrustNotVerified(
        val actorName: String,
        val onMoreInformation: () -> Unit,
    ) : BadgeBottomSheetUiState

    data class VerifiedCheckApp(
        val actorName: String,
        val onMoreInformation: () -> Unit,
    ) : BadgeBottomSheetUiState

    data class NotVerifiedCheckApp(
        val actorName: String,
        val onMoreInformation: () -> Unit,
    ) : BadgeBottomSheetUiState

    data class TrustNotInSystem(
        val actorName: String,
        val onMoreInformation: () -> Unit,
    ) : BadgeBottomSheetUiState

    data class LegitimateIssuer(
        val actorName: String,
        val onMoreInformation: () -> Unit,
    ) : BadgeBottomSheetUiState

    data class NonLegitimateIssuer(
        val actorName: String,
        val onMoreInformation: () -> Unit,
    ) : BadgeBottomSheetUiState

    data class LegitimateVerifier(
        val actorName: String,
        val onMoreInformation: () -> Unit,
    ) : BadgeBottomSheetUiState

    data class NonLegitimateVerifier(
        val actorName: String,
        val onMoreInformation: () -> Unit,
    ) : BadgeBottomSheetUiState

    data class NonCompliantActor(
        val actorName: String,
        val reason: String?,
        val onMoreInformation: () -> Unit,
    ) : BadgeBottomSheetUiState

    data class SensitiveClaimInfo(
        val claimLabel: String,
        val onMoreInformation: () -> Unit,
    ) : BadgeBottomSheetUiState

    data class NonSensitiveClaimInfo(
        val claimLabel: String,
        val onMoreInformation: () -> Unit,
    ) : BadgeBottomSheetUiState

    object SensitiveClaim : BadgeBottomSheetUiState
}

fun BadgeType.ActorInfoBadge.toBadgeBottomSheetUiState(
    actorName: String,
    reason: String? = null,
    onMoreInformation: (Int) -> Unit,
): BadgeBottomSheetUiState = when (this) {
    BadgeType.ActorInfoBadge.LegitimateIssuer -> BadgeBottomSheetUiState.LegitimateIssuer(
        actorName = actorName,
        onMoreInformation = { onMoreInformation(R.string.tk_badgeInformation_furtherInformation_link_value) },
    )

    BadgeType.ActorInfoBadge.LegitimateVerifier -> BadgeBottomSheetUiState.LegitimateVerifier(
        actorName = actorName,
        onMoreInformation = { onMoreInformation(R.string.tk_badgeInformation_furtherInformation_link_value) },
    )

    BadgeType.ActorInfoBadge.NonCompliantActor -> BadgeBottomSheetUiState.NonCompliantActor(
        actorName = actorName,
        reason = reason,
        onMoreInformation = { onMoreInformation(R.string.tk_badgeInformation_furtherInformation_link_value) },
    )

    BadgeType.ActorInfoBadge.NonLegitimateIssuer -> BadgeBottomSheetUiState.NonLegitimateIssuer(
        actorName = actorName,
        onMoreInformation = { onMoreInformation(R.string.tk_badgeInformation_furtherInformation_link_value) },
    )

    BadgeType.ActorInfoBadge.NonLegitimateVerifier -> BadgeBottomSheetUiState.NonLegitimateVerifier(
        actorName = actorName,
        onMoreInformation = { onMoreInformation(R.string.tk_badgeInformation_furtherInformation_link_value) },
    )

    BadgeType.ActorInfoBadge.VerifiedTrust -> BadgeBottomSheetUiState.TrustVerified(
        actorName = actorName,
        onMoreInformation = { onMoreInformation(R.string.tk_badgeInformation_furtherInformation_link_value) },
    )

    BadgeType.ActorInfoBadge.VerifiedCheckApp -> BadgeBottomSheetUiState.VerifiedCheckApp(
        actorName = actorName,
        onMoreInformation = { onMoreInformation(R.string.tk_badgeInformation_furtherInformation_link_value) },
    )

    BadgeType.ActorInfoBadge.NotVerifiedCheckApp -> BadgeBottomSheetUiState.NotVerifiedCheckApp(
        actorName = actorName,
        onMoreInformation = { onMoreInformation(R.string.tk_badgeInformation_furtherInformation_link_value) },
    )

    BadgeType.ActorInfoBadge.NotVerifiedTrust -> BadgeBottomSheetUiState.TrustNotVerified(
        actorName = actorName,
        onMoreInformation = { onMoreInformation(R.string.tk_badgeInformation_furtherInformation_link_value) },
    )

    BadgeType.ActorInfoBadge.NotInSystemTrust -> BadgeBottomSheetUiState.TrustNotInSystem(
        actorName = actorName,
        onMoreInformation = { onMoreInformation(R.string.tk_badgeInformation_furtherInformation_link_value) },
    )
}

fun BadgeType.ClaimInfoBadge.toBadgeBottomSheetUiState(
    onMoreInformation: (Int) -> Unit,
): BadgeBottomSheetUiState = when (this) {
    is BadgeType.ClaimInfoBadge.NonSensitiveClaim -> BadgeBottomSheetUiState.NonSensitiveClaimInfo(
        claimLabel = this.claimLabel,
        onMoreInformation = { onMoreInformation(R.string.tk_badgeInformation_furtherInformation_link_value) },
    )

    is BadgeType.ClaimInfoBadge.SensitiveClaim -> BadgeBottomSheetUiState.SensitiveClaimInfo(
        claimLabel = this.claimLabel,
        onMoreInformation = { onMoreInformation(R.string.tk_badgeInformation_furtherInformation_link_value) },
    )
}
