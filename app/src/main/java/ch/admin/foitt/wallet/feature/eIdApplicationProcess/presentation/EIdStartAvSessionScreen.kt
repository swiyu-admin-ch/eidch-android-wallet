package ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.ScreenMainImage
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun EIdStartAvSessionScreen(viewModel: EIdStartAvSessionViewModel) {
    EIdEidStartAvSessionScreenContent(
        onStart = viewModel::onStart
    )
}

@Composable
private fun EIdEidStartAvSessionScreenContent(
    onStart: () -> Unit,
) = WalletLayouts.ScrollableColumnWithPicture(
    stickyStartContent = {
        ScreenMainImage(
            iconRes = R.drawable.wallet_ic_hand_colored,
            backgroundColor = WalletTheme.colorScheme.surfaceContainerLow
        )
    },
    stickyBottomBackgroundColor = Color.Transparent,
    stickyBottomContent = {
        Buttons.FilledPrimary(
            text = stringResource(R.string.tk_getEid_startAvSession_button_start),
            onClick = onStart,
        )
    },
) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleScreen(
        text = stringResource(R.string.tk_getEid_startAvSession_primary)
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.BodyLarge(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(R.string.tk_getEid_startAvSession_secondary)
    )
    Spacer(modifier = Modifier.height(Sizes.s04))
    WalletTexts.BodySmall(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(R.string.tk_getEid_startAvSession_tertiary)
    )
}

@Composable
@WalletAllScreenPreview
private fun EIdStartAvSessionPreview() {
    WalletTheme {
        EIdEidStartAvSessionScreenContent(
            onStart = {},
        )
    }
}
