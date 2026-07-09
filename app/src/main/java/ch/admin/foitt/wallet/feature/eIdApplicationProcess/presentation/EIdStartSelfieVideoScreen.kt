package ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.AdaptiveBottomButtonBar
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.ScreenMainAnimation
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
internal fun EIdStartSelfieVideoScreen(viewModel: EIdStartSelfieVideoViewModel) {
    BackHandler(enabled = true, viewModel::onClose)

    EIdStartSelfieVideoScreenContent(
        onStart = viewModel::onStart,
    )
}

@Composable
private fun EIdStartSelfieVideoScreenContent(
    onStart: () -> Unit,
) = WalletLayouts.ScrollableColumnWithPicture(
    stickyStartContent = {
        ScreenMainAnimation(
            animationRes = R.raw.face_scan,
            fallbackImage = R.drawable.wallet_ic_selfie_colored,
        )
    },
    stickyBottomContent = {
        AdaptiveBottomButtonBar(
            buttons = listOf(
                {
                    Buttons.FilledPrimary(
                        text = stringResource(R.string.tk_getEid_startSelfieVideo_button_start),
                        onClick = onStart,
                    )
                },
            ),
        )
    },
) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleScreen(
        text = stringResource(R.string.tk_getEid_startSelfieVideo_primary)
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.BodyLarge(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(R.string.tk_getEid_startSelfieVideo_secondary)
    )
}

@Composable
@WalletAllScreenPreview
private fun EIdStartSelfieVideoPreview() {
    WalletTheme {
        EIdStartSelfieVideoScreenContent(
            onStart = {},
        )
    }
}
