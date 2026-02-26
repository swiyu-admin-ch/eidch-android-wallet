package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
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
internal fun EIdDocumentScannerInfoScreen(
    viewModel: EIdDocumentScannerInfoViewModel,
) {
    EIdDocumentScannerInfoScreenContent(
        onContinue = viewModel::onContinue,
    )
}

@Composable
private fun EIdDocumentScannerInfoScreenContent(
    onContinue: () -> Unit,
) = WalletLayouts.ScrollableColumnWithPicture(
    stickyStartContent = {
        ScreenMainImage(
            iconRes = R.drawable.wallet_ic_docscan,
            backgroundColor = WalletTheme.colorScheme.surfaceContainerLow,
            fillMaxWidth = 0.75f,
            paddingValues = PaddingValues(vertical = Sizes.s04)
        )
    },
    stickyBottomBackgroundColor = Color.Transparent,
    stickyBottomContent = {
        Buttons.FilledPrimary(
            text = stringResource(R.string.tk_eidRequest_scanDocument_information_button_primary),
            onClick = onContinue,
        )
    }
) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleScreen(
        text = stringResource(R.string.tk_eidRequest_scanDocument_information_primary)
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.BodyLarge(stringResource(R.string.tk_eidRequest_scanDocument_information_secondary))
}

@WalletAllScreenPreview
@Composable
private fun EIdDocumentScannerInfoScreenPreview() {
    WalletTheme {
        EIdDocumentScannerInfoScreenContent(
            onContinue = {},
        )
    }
}
