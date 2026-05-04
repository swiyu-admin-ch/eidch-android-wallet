package ch.admin.foitt.wallet.platform.actorMetadata.presentation.model

import androidx.compose.ui.graphics.painter.Painter
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.ActorComplianceState
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus

data class ActorUiState(
    val name: String?,
    val painter: Painter?,
    val trustStatus: TrustStatus,
    val vcSchemaTrustStatus: VcSchemaTrustStatus,
    val actorType: ActorType,
    val actorComplianceState: ActorComplianceState,
    val nonComplianceReason: String?,
) {
    companion object {
        val EMPTY = ActorUiState(
            name = null,
            painter = null,
            trustStatus = TrustStatus.UNKNOWN,
            vcSchemaTrustStatus = VcSchemaTrustStatus.UNPROTECTED,
            actorType = ActorType.UNKNOWN,
            actorComplianceState = ActorComplianceState.UNKNOWN,
            nonComplianceReason = null,
        )
    }
}
