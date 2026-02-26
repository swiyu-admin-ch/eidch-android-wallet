package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.model.SDKInfoState
import ch.admin.foitt.wallet.platform.composables.ScanInfoToast
import ch.admin.foitt.wallet.theme.FadingVisibility
import ch.admin.foitt.wallet.theme.Sizes

@Composable
internal fun ScannerInfoBox(
    infoState: SDKInfoState,
    infoText: Int?,
    modifier: Modifier,
) = Box(
    modifier = modifier
        .padding(all = Sizes.s04)
        .navigationBarsPadding()
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
