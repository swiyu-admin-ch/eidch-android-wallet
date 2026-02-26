package ch.admin.foitt.wallet.feature.onboarding.presentation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.presentation.ScreenMainImage
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.utils.TestTags
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun OnboardingSuccessScreen(
    viewModel: OnboardingSuccessViewModel,
) {
    OnboardingSuccessScreenContent(
        isLoading = viewModel.isLoading.collectAsStateWithLifecycle().value,
        onNext = viewModel::onNext,
    )
}

@Composable
private fun OnboardingSuccessScreenContent(
    isLoading: Boolean,
    onNext: () -> Unit,
) {
    WalletLayouts.ScrollableColumnWithPicture(
        stickyStartContent = {
            ScreenMainImage(
                modifier = Modifier.testTag(TestTags.SUCCESS_ICON.name),
                iconRes = R.drawable.wallet_ic_check_circle_colored,
                backgroundColor = WalletTheme.colorScheme.surfaceContainerLow,
            )
        },
        stickyBottomContent = {
            Buttons.FilledPrimary(
                text = stringResource(id = R.string.tk_global_continue),
                onClick = onNext,
                modifier = Modifier.testTag(TestTags.CONTINUE_BUTTON.name)
            )
        }
    ) {
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.TitleScreen(
            text = stringResource(id = R.string.tk_onboarding_done_primary),
        )
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.BodyLarge(
            text = stringResource(id = R.string.tk_onboarding_done_secondary),
            modifier = Modifier.fillMaxWidth(),
        )
    }
    LoadingOverlay(
        showOverlay = isLoading
    )
}

@WalletAllScreenPreview
@Composable
private fun OnboardingSuccessScreenPreview() {
    WalletTheme {
        OnboardingSuccessScreenContent(
            isLoading = false,
            onNext = {},
        )
    }
}
