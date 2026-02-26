package ch.admin.foitt.wallet.feature.onboarding.presentation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
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
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun OnboardingLocalDataScreen(
    viewModel: OnboardingLocalDataViewModel,
) {
    SwipeableScreen(
        onSwipeForward = viewModel::onNext,
        onSwipeBackWard = viewModel::onBack,
    ) {
        OnboardingLocalDataScreenContent(
            onMoreInformation = viewModel::onMoreInformation,
            onNext = viewModel::onNext,
        )
    }
}

@Composable
private fun OnboardingLocalDataScreenContent(
    onMoreInformation: () -> Unit,
    onNext: () -> Unit,
) = WalletLayouts.ScrollableColumnWithPicture(
    stickyStartContent = {
        ScreenMainImage(
            modifier = Modifier.testTag(TestTags.LOCAL_DATA_ICON.name),
            iconRes = R.drawable.wallet_ic_shield_person,
            backgroundRes = R.drawable.wallet_background_gradient_07,
        )
    },
    stickyBottomContent = {
        Buttons.FilledPrimary(
            modifier = Modifier.testTag(TestTags.CONTINUE_BUTTON.name),
            text = stringResource(id = R.string.tk_global_continue),
            onClick = onNext,
        )
    }
) {
    OnboardingScreenContent(
        title = stringResource(id = R.string.tk_onboarding_introductionStep_yourData_primary),
        subtitle = stringResource(id = R.string.tk_onboarding_introductionStep_yourData_secondary),
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    Buttons.TextLink(
        text = stringResource(id = R.string.tk_onboarding_introductionStep_yourData_tertiary_link_text),
        onClick = onMoreInformation,
        endIcon = painterResource(id = R.drawable.wallet_ic_chevron),
    )
}

@WalletAllScreenPreview
@Composable
private fun OnboardingLocalDataScreenPreview() {
    WalletTheme {
        OnboardingLocalDataScreenContent(
            onMoreInformation = {},
            onNext = {},
        )
    }
}
