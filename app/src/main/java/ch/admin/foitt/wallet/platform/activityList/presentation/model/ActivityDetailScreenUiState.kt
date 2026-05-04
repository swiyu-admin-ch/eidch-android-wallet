package ch.admin.foitt.wallet.platform.activityList.presentation.model

import androidx.compose.ui.graphics.Color
import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityType
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialDisplayStatus
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.ActorComplianceState
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimCluster
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus

data class ActivityDetailScreenUiState(
    val activity: ActivityDetailUiState,
    val credential: CredentialCardState,
    val claims: List<CredentialClaimCluster>,
) {
    companion object {
        val EMPTY = ActivityDetailScreenUiState(
            credential = CredentialCardState(
                credentialId = -1,
                status = CredentialDisplayStatus.Unknown,
                title = "",
                subtitle = null,
                logo = null,
                backgroundColor = Color.Transparent,
                contentColor = Color.Transparent,
                borderColor = Color.Transparent,
                isCredentialFromBetaIssuer = false
            ),
            activity = ActivityDetailUiState(
                id = -1,
                activityType = ActivityType.ISSUANCE,
                date = "01.01.1970 | 00:00",
                localizedActorName = "",
                actorImage = null,
                actorTrust = TrustStatus.UNKNOWN,
                vcSchemaTrust = VcSchemaTrustStatus.UNPROTECTED,
                actorCompliance = ActorComplianceState.UNKNOWN,
            ),
            claims = emptyList(),
        )
    }
}
