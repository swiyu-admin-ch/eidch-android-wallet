package ch.admin.foitt.wallet.feature.onboarding.presentation

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.AdaptiveBottomButtonBar
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.ScreenMainImage
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.utils.LocalActivity
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun OnboardingFatalErrorScreen(
    viewModel: OnboardingFatalErrorViewModel
) {
    val currentActivity = LocalActivity.current
    BackHandler(onBack = currentActivity::finish)

    OnboardingFatalErrorScreenContent(
        primaryText = viewModel.primaryTextRes,
        secondaryText = viewModel.secondaryTextRes,
        onClose = currentActivity::finish,
    )
}

@Composable
private fun OnboardingFatalErrorScreenContent(
    @StringRes primaryText: Int,
    @StringRes secondaryText: Int,
    onClose: () -> Unit,
) {
    WalletLayouts.ScrollableColumnWithPicture(
        stickyStartContent = {
            ScreenMainImage(
                iconRes = R.drawable.wallet_ic_cross_circle_colored,
                backgroundColor = WalletTheme.colorScheme.surfaceContainerLow,
            )
        },
        stickyBottomContent = {
            AdaptiveBottomButtonBar(
                buttons = listOf(
                    {
                        Buttons.FilledPrimary(
                            text = stringResource(id = R.string.tk_onboarding_failure_button_close),
                            onClick = onClose,
                        )
                    }
                )
            )
        },
    ) {
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.TitleScreen(
            text = stringResource(primaryText)
        )
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.BodyLarge(
            text = stringResource(secondaryText)
        )
    }
}

@WalletAllScreenPreview
@Composable
private fun OnboardingFatalErrorPreview() {
    WalletTheme {
        OnboardingFatalErrorScreenContent(
            primaryText = R.string.tk_onboarding_failure_passphraseInitialization_primary,
            secondaryText = R.string.tk_onboarding_failure_passphraseInitialization_secondary,
            onClose = {}
        )
    }
}
