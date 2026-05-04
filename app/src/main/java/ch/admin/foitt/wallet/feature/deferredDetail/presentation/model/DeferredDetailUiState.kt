package ch.admin.foitt.wallet.feature.deferredDetail.presentation.model

import androidx.compose.ui.graphics.Color
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.ActorComplianceState
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus

data class DeferredDetailUiState(
    val credential: CredentialCardState,
    val issuer: ActorUiState
) {
    companion object {
        val EMPTY = DeferredDetailUiState(
            credential = CredentialCardState(
                credentialId = -1,
                status = null,
                title = "",
                subtitle = null,
                logo = null,
                backgroundColor = Color.Transparent,
                contentColor = Color.Transparent,
                borderColor = Color.Transparent,
                isCredentialFromBetaIssuer = false,
                deferredStatus = null
            ),
            issuer = ActorUiState(
                name = null,
                painter = null,
                trustStatus = TrustStatus.UNKNOWN,
                vcSchemaTrustStatus = VcSchemaTrustStatus.TRUSTED,
                actorType = ActorType.UNKNOWN,
                actorComplianceState = ActorComplianceState.UNKNOWN,
                nonComplianceReason = null,
            )
        )
    }
}
