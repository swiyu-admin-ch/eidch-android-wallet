package ch.admin.foitt.wallet.feature.onboarding.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.onboarding.presentation.composables.OnboardingScreenContent
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.ScreenMainImage
import ch.admin.foitt.wallet.platform.composables.presentation.SwipeableScreen
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun OnboardingConfirmPassphraseFailureScreen(
    viewModel: OnboardingPassphraseConfirmationFailureViewModel,
) {
    SwipeableScreen(
        onSwipeForward = {},
        onSwipeBackWard = viewModel::onBack,
    ) {
        OnboardingPassphraseConfirmationFailedScreenContent(
            onBack = viewModel::onBack
        )
    }
}

@Composable
private fun OnboardingPassphraseConfirmationFailedScreenContent(
    onBack: () -> Unit,
) = WalletLayouts.ScrollableColumnWithPicture(
    stickyStartContent = {
        ScreenMainImage(
            modifier = Modifier.testTag("passphraseConfirmationFailedImage"),
            iconRes = R.drawable.wallet_ic_lock,
            backgroundRes = R.drawable.wallet_background_gradient_04,
        )
    },
    stickyBottomContent = {
        Buttons.FilledPrimary(
            modifier = Modifier.testTag("back"),
            text = stringResource(id = R.string.tk_onboarding_passworderror_primarybutton),
            onClick = onBack,
        )
    }
) {
    ScrollableContent()
}

@Composable
private fun ScrollableContent() {
    OnboardingScreenContent(
        title = stringResource(id = R.string.tk_onboarding_passworderror_title),
        subtitle = stringResource(id = R.string.tk_onboarding_passwordIntroduction_error_tooManyAttempts),
    )
}

@WalletAllScreenPreview
@Composable
private fun OnboardingPassphraseConfirmationFailedScreenPreview() {
    WalletTheme {
        OnboardingPassphraseConfirmationFailedScreenContent(
            onBack = {},
        )
    }
}
