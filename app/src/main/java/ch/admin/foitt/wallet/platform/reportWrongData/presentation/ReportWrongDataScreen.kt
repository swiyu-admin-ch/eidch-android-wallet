package ch.admin.foitt.wallet.platform.reportWrongData.presentation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.presentation.ScreenMainImage
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.utils.TestTags
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun ReportWrongDataScreen() {
    ReportWrongDataScreenContent()
}

@Composable
private fun ReportWrongDataScreenContent() = WalletLayouts.ScrollableColumnWithPicture(
    stickyStartContent = {
        ScreenMainImage(
            iconRes = R.drawable.wallet_ic_cross_circle_colored,
            backgroundColor = WalletTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.testTag(TestTags.WRONG_DATA_IMAGE.name)
        )
    },
    stickyBottomContent = null
) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleLarge(
        text = stringResource(id = R.string.tk_receive_credentialOffer_wrongData_primary),
        modifier = Modifier.testTag(TestTags.WRONG_DATA_TITLE.name)
    )
    Spacer(modifier = Modifier.height(Sizes.s05))
    WalletTexts.BodyLarge(
        text = stringResource(id = R.string.tk_receive_credentialOffer_wrongData_secondary),
        modifier = Modifier.fillMaxWidth(),
    )
}

@WalletAllScreenPreview
@Composable
private fun ReportWrongDataScreenPreview() {
    WalletTheme {
        ReportWrongDataScreenContent()
    }
}
