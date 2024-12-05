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
import com.ramcosta.composedestinations.annotation.Destination

@Destination
@Composable
fun OnboardingLocalDataScreen(
    viewModel: OnboardingLocalDataViewModel,
) {
    SwipeableScreen(
        onSwipeForward = viewModel::onNext,
        onSwipeBackWard = viewModel::onBack,
    ) {
        OnboardingLocalDataScreenContent(
            onNext = viewModel::onNext,
        )
    }
}

@Composable
private fun OnboardingLocalDataScreenContent(
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
            text = stringResource(id = R.string.global_continue),
            onClick = onNext,
        )
    }
) {
    OnboardingScreenContent(
        title = stringResource(id = R.string.onboarding_security_primary),
        subtitle = stringResource(id = R.string.onboarding_security_secondary),
        details = stringResource(id = R.string.onboarding_security_details),
    )
}

@WalletAllScreenPreview
@Composable
private fun OnboardingLocalDataScreenPreview() {
    WalletTheme {
        OnboardingLocalDataScreenContent(
            onNext = {},
        )
    }
}
