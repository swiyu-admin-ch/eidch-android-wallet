package ch.admin.foitt.wallet.feature.login.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.centerHorizontallyOnFullscreen
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithFullscreenGradient
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.utils.LocalActivity
import ch.admin.foitt.wallet.platform.utils.OnResumeEventHandler
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun UnsecuredDeviceScreen(viewModel: UnsecuredDeviceViewModel) {
    val activity = LocalActivity.current
    BackHandler {
        activity.finish()
    }
    OnResumeEventHandler {
        viewModel.checkHasSecureLockScreen()
    }
    UnsecuredDeviceScreenContent(
        onSettings = viewModel::goToSettings
    )
}

@Composable
private fun UnsecuredDeviceScreenContent(onSettings: () -> Unit) {
    WalletLayouts.ScrollableColumnWithFullscreenGradient(
        stickyBottomContent = {
            Buttons.FilledPrimaryFixed(
                text = stringResource(R.string.tk_unsecuredDevice_button_settings),
                onClick = onSettings,
            )
        },
        scrollableContent = {
            Column(
                modifier = Modifier.centerHorizontallyOnFullscreen(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    modifier = Modifier.height(Sizes.s20),
                    painter = painterResource(R.drawable.wallet_ic_shield_cross),
                    tint = WalletTheme.colorScheme.onGradientFixed,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.height(Sizes.s06))
                WalletTexts.TitleLarge(
                    text = stringResource(R.string.tk_unsecuredDevice_primary),
                    textAlign = TextAlign.Center,
                    color = WalletTheme.colorScheme.onGradientFixed,
                )
                Spacer(modifier = Modifier.height(Sizes.s02))
                WalletTexts.BodyLarge(
                    text = stringResource(R.string.tk_unsecuredDevice_secondary),
                    textAlign = TextAlign.Center,
                    color = WalletTheme.colorScheme.onGradientFixed,
                )
                Spacer(modifier = Modifier.height(Sizes.s06))
                WalletTexts.BodyLarge(
                    text = stringResource(R.string.tk_unsecuredDevice_tertiary),
                    textAlign = TextAlign.Center,
                    color = WalletTheme.colorScheme.onGradientFixed,
                )
            }
        },
    )
}

@WalletAllScreenPreview
@Composable
private fun UnsecuredDeviceScreenPreview() {
    WalletTheme {
        UnsecuredDeviceScreenContent(onSettings = {})
    }
}
