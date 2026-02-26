package ch.admin.foitt.wallet.platform.credential.presentation.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialDisplayStatus
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredProgressionState
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableProgressionState

data class CredentialCardState(
    val credentialId: Long,
    val title: String?,
    val subtitle: String?,
    val status: CredentialDisplayStatus?,
    val logo: Painter?,
    val backgroundColor: Color,
    val contentColor: Color,
    val borderColor: Color,
    val isCredentialFromBetaIssuer: Boolean,
    val progressionState: VerifiableProgressionState = VerifiableProgressionState.UNACCEPTED,
    val deferredStatus: DeferredProgressionState? = null,
) {
    val useDefaultBackground = backgroundColor == defaultCardColor

    val isDeferred = deferredStatus != null
    val isUnaccepted = progressionState == VerifiableProgressionState.UNACCEPTED

    companion object {
        val defaultCardColor = Color(0xFFF0EDE5)
    }
}
