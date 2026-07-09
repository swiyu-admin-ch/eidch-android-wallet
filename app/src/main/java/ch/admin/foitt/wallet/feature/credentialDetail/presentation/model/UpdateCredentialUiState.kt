package ch.admin.foitt.wallet.feature.credentialDetail.presentation.model

import androidx.compose.ui.graphics.painter.Painter

data class UpdateCredentialUiState(
    val issuerName: String? = null,
    val issuerPainter: Painter? = null,
)
