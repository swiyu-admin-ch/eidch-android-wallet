package ch.admin.foitt.wallet.platform.badges.domain.model

sealed interface BadgeType {
    sealed interface ActorInfoBadge : BadgeType {
        object VerifiedTrust : ActorInfoBadge
        object NotVerifiedTrust : ActorInfoBadge
        object VerifiedCheckApp : ActorInfoBadge
        object NotVerifiedCheckApp : ActorInfoBadge
        object NotInSystemTrust : ActorInfoBadge
        object LegitimateIssuer : ActorInfoBadge
        object NonLegitimateIssuer : ActorInfoBadge
        object LegitimateVerifier : ActorInfoBadge
        object NonLegitimateVerifier : ActorInfoBadge
        object NonCompliantActor : ActorInfoBadge
    }

    sealed interface ClaimInfoBadge : BadgeType {
        data class NonSensitiveClaim(val claimLabel: String) : ClaimInfoBadge
        data class SensitiveClaim(val claimLabel: String) : ClaimInfoBadge
    }
}
