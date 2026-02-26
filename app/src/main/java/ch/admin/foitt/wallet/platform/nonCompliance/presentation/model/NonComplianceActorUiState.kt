package ch.admin.foitt.wallet.platform.nonCompliance.presentation.model

import androidx.compose.ui.graphics.painter.Painter

data class NonComplianceActorUiState(
    val name: String,
    val logo: Painter?,
) {
    companion object {
        val EMPTY = NonComplianceActorUiState(
            name = "",
            logo = null,
        )
    }
}
