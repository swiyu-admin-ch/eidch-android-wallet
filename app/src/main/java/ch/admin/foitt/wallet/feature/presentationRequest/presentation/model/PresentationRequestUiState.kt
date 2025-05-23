package ch.admin.foitt.wallet.feature.presentationRequest.presentation.model

import androidx.compose.ui.graphics.Color
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialDisplayStatus
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimData

data class PresentationRequestUiState(
    val credential: CredentialCardState,
    val requestedClaims: List<CredentialClaimData>,
) {
    companion object {
        val EMPTY by lazy {
            PresentationRequestUiState(
                credential = CredentialCardState(
                    credentialId = -1,
                    status = CredentialDisplayStatus.Unknown,
                    title = "",
                    subtitle = null,
                    logo = null,
                    backgroundColor = Color.Unspecified,
                    contentColor = Color.Unspecified,
                    borderColor = Color.Unspecified,
                    isCredentialFromBetaIssuer = false,
                ),
                requestedClaims = emptyList(),
            )
        }
    }
}
