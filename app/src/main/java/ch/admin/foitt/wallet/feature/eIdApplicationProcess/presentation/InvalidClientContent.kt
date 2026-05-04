package ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.ScreenMainImage
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun InvalidClientContent(
    onClose: () -> Unit,
    onHelp: () -> Unit,
    onPlaystore: () -> Unit,
) = WalletLayouts.ScrollableColumnWithPicture(
    stickyStartContent = {
        ScreenMainImage(
            iconRes = R.drawable.wallet_ic_cross_circle_colored,
            backgroundColor = WalletTheme.colorScheme.surfaceContainerLow
        )
    },
    stickyBottomBackgroundColor = Color.Transparent,
    stickyBottomContent = {
        Buttons.FilledPrimary(
            text = stringResource(R.string.tk_eidRequest_attestation_clientNotSupported_button_playstore_text),
            onClick = onPlaystore,
            modifier = Modifier
                .fillMaxWidth()
        )
        Buttons.TonalSecondary(
            text = stringResource(R.string.tk_eidRequest_attestation_clientNotSupported_button_close),
            onClick = onClose,
            modifier = Modifier
                .fillMaxWidth()
        )
    },
) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleScreen(
        text = stringResource(id = R.string.tk_eidRequest_attestation_clientNotSupported_primary),
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.BodyLarge(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(id = R.string.tk_eidRequest_attestation_clientNotSupported_secondary),
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    Buttons.TextLink(
        text = stringResource(id = R.string.tk_eidRequest_attestation_clientNotSupported_link_text),
        onClick = onHelp,
        endIcon = painterResource(id = R.drawable.wallet_ic_chevron),
    )
}
