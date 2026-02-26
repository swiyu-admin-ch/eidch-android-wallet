package ch.admin.foitt.wallet.feature.home.presentation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
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
fun BetaIdScreen(
    viewModel: BetaIdViewModel,
) {
    EIdIntroScreenContent(
        onBetaIdButtonClick = viewModel::onBetaIdButtonClick,
    )
}

@Composable
private fun EIdIntroScreenContent(
    onBetaIdButtonClick: () -> Unit,
) {
    WalletLayouts.ScrollableColumnWithPicture(
        stickyStartContent = {
            ScreenMainImage(
                iconRes = R.drawable.wallet_ic_credential_add_colored,
                backgroundColor = WalletTheme.colorScheme.surfaceContainerLow
            )
        },
        stickyBottomContent = {
            Buttons.FilledPrimary(
                text = stringResource(id = R.string.tk_global_getbetaid_primarybutton),
                onClick = onBetaIdButtonClick,
                startIcon = painterResource(id = R.drawable.wallet_ic_external_link)
            )
        }
    ) {
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.TitleScreen(
            text = stringResource(id = R.string.tk_getBetaId_create_title),
        )
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.BodyLarge(
            text = stringResource(id = R.string.tk_getBetaId_create_body),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@WalletAllScreenPreview
@Composable
private fun BetaIdScreenPreview() {
    WalletTheme {
        EIdIntroScreenContent(
            onBetaIdButtonClick = {},
        )
    }
}
