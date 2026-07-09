package ch.admin.foitt.wallet.feature.settings.presentation.biometrics

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.biometrics.presentation.BiometricsContent
import ch.admin.foitt.wallet.platform.biometrics.presentation.BiometricsUnavailableImage
import ch.admin.foitt.wallet.platform.composables.AdaptiveBottomButtonBar
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun EnableBiometricsLockoutScreen(viewModel: EnableBiometricsLockoutViewModel) {
    EnableBiometricsLockoutContent(
        onClose = viewModel::onClose,
    )
}

@Composable
private fun EnableBiometricsLockoutContent(
    onClose: () -> Unit,
) = WalletLayouts.ScrollableColumnWithPicture(
    stickyStartContent = {
        BiometricsUnavailableImage()
    },
    stickyBottomContent = {
        AdaptiveBottomButtonBar(
            buttons = listOf(
                {
                    Buttons.FilledPrimary(
                        text = stringResource(id = R.string.global_error_backToHome_button),
                        onClick = onClose,
                    )
                },
            ),
        )
    },
    content = {
        BiometricsContent(
            title = R.string.biometrics_lockout_title,
            description = R.string.biometrics_lockout_text,
            infoText = null,
        )
    },
)

@WalletAllScreenPreview
@Composable
private fun EnableBiometricsLockoutPreview() {
    WalletTheme {
        EnableBiometricsLockoutContent(
            onClose = {},
        )
    }
}
