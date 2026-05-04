package ch.admin.foitt.wallet.feature.credentialDetail.presentation.model

import androidx.compose.ui.graphics.Color
import ch.admin.foitt.wallet.platform.activityList.presentation.model.ActivityUiState
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialDisplayStatus
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.ActorComplianceState
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimCluster
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus

data class CredentialDetailUiState(
    val credential: CredentialCardState,
    val clusterItems: List<CredentialClaimCluster>,
    val issuer: ActorUiState,
    val areActivitiesEnabled: Boolean,
    val activities: List<ActivityUiState>,
) {
    companion object {
        val EMPTY = CredentialDetailUiState(
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
            clusterItems = emptyList(),
            issuer = ActorUiState(
                name = null,
                painter = null,
                trustStatus = TrustStatus.UNKNOWN,
                vcSchemaTrustStatus = VcSchemaTrustStatus.TRUSTED,
                actorType = ActorType.UNKNOWN,
                actorComplianceState = ActorComplianceState.UNKNOWN,
                nonComplianceReason = null,
            ),
            areActivitiesEnabled = true,
            activities = emptyList(),
        )
    }
}
