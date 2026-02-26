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
import ch.admin.foitt.wallet.platform.utils.TestTags
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun OnboardingPassphraseExplanationScreen(
    viewModel: OnboardingPassphraseExplanationViewModel,
) {
    SwipeableScreen(
        onSwipeForward = viewModel::onNext,
        onSwipeBackWard = viewModel::onBack,
    ) {
        OnboardingPassphraseExplanationScreenContent(
            onNext = viewModel::onNext,
        )
    }
}

@Composable
private fun OnboardingPassphraseExplanationScreenContent(
    onNext: () -> Unit,
) = WalletLayouts.ScrollableColumnWithPicture(
    stickyStartContent = {
        ScreenMainImage(
            modifier = Modifier.testTag(TestTags.PIN_EXPLANATION_ICON.name),
            iconRes = R.drawable.wallet_ic_lock,
            backgroundRes = R.drawable.wallet_background_gradient_04,
        )
    },
    stickyBottomContent = {
        Buttons.FilledPrimary(
            modifier = Modifier.testTag(TestTags.CONTINUE_BUTTON.name),
            text = stringResource(id = R.string.tk_onboarding_passwordIntroduction_button_primary),
            onClick = onNext,
        )
    }
) {
    OnboardingScreenContent(
        title = stringResource(id = R.string.tk_onboarding_passwordIntroduction_primary),
        subtitle = stringResource(id = R.string.tk_onboarding_passwordIntroduction_secondary),
    )
}

@WalletAllScreenPreview
@Composable
private fun OnboardingPassphraseExplanationScreenPreview() {
    WalletTheme {
        OnboardingPassphraseExplanationScreenContent(
            onNext = {},
        )
    }
}
