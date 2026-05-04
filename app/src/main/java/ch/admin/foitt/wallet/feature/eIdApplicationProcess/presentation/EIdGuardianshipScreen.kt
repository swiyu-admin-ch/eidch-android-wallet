package ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation

import androidx.activity.compose.BackHandler
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
fun EIdGuardianshipScreen(
    viewModel: EIdGuardianshipViewModel,
) {
    BackHandler {
        viewModel.shutDownLibraryAndGoBack()
    }

    EIdGuardianshipScreenContent(
        onDeclareGuardianship = viewModel::onDeclareGuardianship,
    )
}

@Composable
private fun EIdGuardianshipScreenContent(
    onDeclareGuardianship: (Boolean) -> Unit,
) {
    WalletLayouts.ScrollableColumnWithPicture(
        stickyStartContent = {
            ScreenMainImage(
                iconRes = R.drawable.wallet_ic_person_circle_colored,
                backgroundColor = WalletTheme.colorScheme.surfaceContainerLow,
            )
        },
        stickyBottomBackgroundColor = Color.Transparent,
        stickyBottomContent = {
            Buttons.FilledPrimary(
                text = stringResource(R.string.tk_getEid_guardianship_button_no),
                onClick = { onDeclareGuardianship(false) },
                modifier = Modifier.fillMaxWidth(),
            )
            Buttons.TonalSecondary(
                text = stringResource(R.string.tk_getEid_guardianship_button_yes),
                onClick = { onDeclareGuardianship(true) },
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.TitleScreen(
            text = stringResource(id = R.string.tk_getEid_guardianship_primary)
        )
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.BodyLarge(
            text = stringResource(id = R.string.tk_getEid_guardianship_secondary)
        )
    }
}

@WalletAllScreenPreview
@Composable
private fun EIdGuardianshipScreenPreview() {
    WalletTheme {
        EIdGuardianshipScreenContent(
            onDeclareGuardianship = {},
        )
    }
}
