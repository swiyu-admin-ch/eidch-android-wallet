package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.model.SDKInfoState
import ch.admin.foitt.wallet.platform.composables.ScanInfoToast
import ch.admin.foitt.wallet.theme.FadingVisibility

@Composable
internal fun ScannerInfoBox(
    infoState: SDKInfoState,
    infoText: Int?,
    modifier: Modifier,
) = Box(
    modifier = modifier
) {
    FadingVisibility(visible = infoState != SDKInfoState.Empty && infoText != null) {
        infoText?.let {
            ScanInfoToast(
                modifier = Modifier,
                text = it
            )
        }
    }
}
