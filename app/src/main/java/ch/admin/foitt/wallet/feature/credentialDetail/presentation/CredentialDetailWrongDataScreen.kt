package ch.admin.foitt.wallet.feature.credentialDetail.presentation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.presentation.ScreenMainImage
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun CredentialDetailWrongDataScreen() {
    CredentialDetailWrongDataScreenContent()
}

@Composable
private fun CredentialDetailWrongDataScreenContent() = WalletLayouts.ScrollableColumnWithPicture(
    stickyStartContent = {
        ScreenMainImage(
            iconRes = R.drawable.wallet_ic_cross_circle_colored,
            backgroundColor = WalletTheme.colorScheme.surfaceContainerHigh
        )
    },
    stickyBottomContent = null
) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleLarge(
        text = stringResource(id = R.string.tk_displaydelete_wrongdata_title),
    )
    Spacer(modifier = Modifier.height(Sizes.s05))
    WalletTexts.BodyLarge(
        text = stringResource(id = R.string.tk_displaydelete_wrongdata_body),
        modifier = Modifier.fillMaxWidth(),
    )
}

@WalletAllScreenPreview
@Composable
private fun CredentialDetailWrongDataScreenPreview() {
    WalletTheme {
        CredentialDetailWrongDataScreenContent()
    }
}
