package ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.ScreenMainImage
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.utils.TestTags
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun EIdIntroScreen(
    viewModel: EIdIntroViewModel,
) {
    BackHandler {
        viewModel.onBack()
    }

    EIdIntroScreenContent(
        onRequestEId = viewModel::onRequestEId,
        onSkip = viewModel::onSkip,
    )
}

@Composable
private fun EIdIntroScreenContent(
    onRequestEId: () -> Unit,
    onSkip: () -> Unit,
) {
    WalletLayouts.ScrollableColumnWithPicture(
        stickyStartContent = {
            ScreenMainImage(
                modifier = Modifier.testTag(TestTags.EID_INTRO_ICON.name),
                iconRes = R.drawable.wallet_ic_credential_add_colored,
                backgroundColor = WalletTheme.colorScheme.surfaceContainerLow
            )
        },
        stickyBottomBackgroundColor = Color.Transparent,
        stickyBottomContent = {
            Buttons.TonalSecondary(
                modifier = Modifier.testTag(TestTags.DECLINE_BUTTON.name),
                text = stringResource(R.string.tk_getEid_intro_secondaryButton),
                onClick = onSkip,
            )
            Buttons.FilledPrimary(
                modifier = Modifier.testTag(TestTags.ACCEPT_BUTTON.name),
                text = stringResource(R.string.tk_getEid_intro_primaryButton),
                onClick = onRequestEId,
            )
        }
    ) {
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.TitleScreen(
            text = stringResource(id = R.string.tk_getEid_intro_title),
        )
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.BodyLarge(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.tk_getEid_intro_body),
        )
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.BodySmall(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.tk_getEid_intro_smallBody),
        )
    }
}

@WalletAllScreenPreview
@Composable
private fun EIdIntroScreenPreview() {
    WalletTheme {
        EIdIntroScreenContent(
            onRequestEId = {},
            onSkip = {},
        )
    }
}
