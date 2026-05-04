package ch.admin.foitt.wallet.feature.onboarding.presentation

import androidx.compose.runtime.Composable
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
fun OnboardingActivityScreen(
    viewModel: OnboardingActivityViewModel,
) {
    SwipeableScreen(
        onSwipeForward = viewModel::onNext,
        onSwipeBackWard = viewModel::onBack,
    ) {
        OnboardingActivityScreenContent(
            onNext = viewModel::onNext,
        )
    }
}

@Composable
private fun OnboardingActivityScreenContent(
    onNext: () -> Unit,
) = WalletLayouts.ScrollableColumnWithPicture(
    stickyStartContent = {
        ScreenMainImage(
            iconRes = R.drawable.wallet_ic_onboarding_history,
            backgroundRes = R.drawable.wallet_background_gradient_07,
        )
    },
    stickyBottomContent = {
        Buttons.FilledPrimary(
            text = stringResource(id = R.string.tk_global_continue),
            onClick = onNext,
        )
    }
) {
    OnboardingScreenContent(
        title = stringResource(id = R.string.tk_onboarding_introductionStep_activities_primary),
        subtitle = stringResource(id = R.string.tk_onboarding_introductionStep_activities_secondary)
    )
}

@WalletAllScreenPreview
@Composable
private fun OnboardingActivityScreenPreview() {
    WalletTheme {
        OnboardingActivityScreenContent(
            onNext = {},
        )
    }
}
