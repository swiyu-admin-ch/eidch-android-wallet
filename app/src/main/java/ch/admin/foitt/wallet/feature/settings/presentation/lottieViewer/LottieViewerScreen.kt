package ch.admin.foitt.wallet.feature.settings.presentation.lottieViewer

import androidx.annotation.RawRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.LottieIcon
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
internal fun LottieViewerScreen(
    viewModel: LottieViewerViewModel,
) {
    LottieViewerScreenContent(
        animationRes = viewModel.animationRes.collectAsStateWithLifecycle().value,
        animationScaling = viewModel.animationScaling.collectAsStateWithLifecycle().value,
        onNextAnimation = viewModel::onNextAnimation,
    )
}

@Composable
private fun LottieViewerScreenContent(
    @RawRes animationRes: Int,
    animationScaling: ContentScale,
    onNextAnimation: () -> Unit,
) = WalletLayouts.ScrollableColumnWithPicture(
    stickyStartContent = {
        LottieIcon(
            animationRes = animationRes,
            contentScale = animationScaling,
            modifier = Modifier
                .fillMaxSize()
                .clip(WalletTheme.shapes.extraLarge)
                .background(WalletTheme.colorScheme.surfaceContainerLow)
        )
    },
    stickyBottomContent = {
        Buttons.FilledPrimary(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.tk_global_continue_button),
            onClick = onNextAnimation,
        )
    },
) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleScreen(
        text = stringResource(R.string.tk_getEid_startSelfieVideo_primary)
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.BodyLarge(stringResource(R.string.tk_getEid_startSelfieVideo_secondary))
}

@WalletAllScreenPreview
@Composable
private fun LottieViewerPreview() {
    WalletTheme {
        LottieViewerScreenContent(
            animationRes = R.raw.doc_scan,
            animationScaling = ContentScale.Crop,
            onNextAnimation = {},
        )
    }
}
