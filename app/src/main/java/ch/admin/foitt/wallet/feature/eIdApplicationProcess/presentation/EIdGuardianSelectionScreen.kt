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
fun EIdGuardianSelectionScreen(
    viewModel: EIdGuardianSelectionViewModel,
) {
    EIdGuardianSelectionScreenContent(
        onObtainConsent = viewModel::onObtainConsent,
        onContinueAsGuardian = viewModel::onContinueAsGuardian,
    )
}

@Composable
private fun EIdGuardianSelectionScreenContent(
    onObtainConsent: () -> Unit,
    onContinueAsGuardian: () -> Unit,
) {
    WalletLayouts.ScrollableColumnWithPicture(
        stickyStartContent = {
            ScreenMainImage(
                iconRes = R.drawable.wallet_ic_check_circle_colored,
                backgroundColor = WalletTheme.colorScheme.surfaceContainerLow
            )
        },
        stickyBottomBackgroundColor = Color.Transparent,
        stickyBottomContent = {
            Buttons.TonalSecondary(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.tk_getEid_guardianSelection_button_obtainConsent),
                onClick = onObtainConsent,
            )
            Buttons.TonalSecondary(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.tk_getEid_guardianSelection_button_continueAsGuardian),
                onClick = onContinueAsGuardian,
            )
        }
    ) {
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.TitleScreen(
            text = stringResource(id = R.string.tk_getEid_guardianSelection_primary),
        )
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.BodyLarge(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.tk_getEid_guardianSelection_secondary),
        )
    }
}

@WalletAllScreenPreview
@Composable
private fun EIdGuardianSelectionScreenPreview() {
    WalletTheme {
        EIdGuardianSelectionScreenContent(
            onObtainConsent = {},
            onContinueAsGuardian = {},
        )
    }
}
