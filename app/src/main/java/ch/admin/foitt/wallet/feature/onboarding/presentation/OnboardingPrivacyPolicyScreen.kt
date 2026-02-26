package ch.admin.foitt.wallet.feature.onboarding.presentation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
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
fun OnboardingPrivacyPolicyScreen(viewModel: OnboardingPrivacyPolicyViewModel) {
    SwipeableScreen(
        onSwipeForward = {},
        onSwipeBackWard = viewModel::onBack,
    ) {
        UserPrivacyPolicyScreenContent(
            acceptTracking = viewModel::acceptTracking,
            declineTracking = viewModel::declineTracking,
            onOpenUserPrivacyPolicyLink = viewModel::onOpenUserPrivacyPolicy,
        )
    }
}

@Composable
private fun UserPrivacyPolicyScreenContent(
    acceptTracking: () -> Unit,
    declineTracking: () -> Unit,
    onOpenUserPrivacyPolicyLink: () -> Unit,
) = WalletLayouts.ScrollableColumnWithPicture(
    stickyStartContent = {
        ScreenMainImage(
            modifier = Modifier.testTag(TestTags.USER_PRIVACY_ICON.name),
            iconRes = R.drawable.wallet_ic_verify_cross,
            backgroundRes = R.drawable.wallet_background_gradient_06,
        )
    },
    stickyBottomContent = {
        Buttons.TonalSecondary(
            text = stringResource(id = R.string.tk_onboarding_analytics_button_secondary),
            onClick = declineTracking,
            modifier = Modifier
                .semantics {
                    traversalIndex = 1f
                }.testTag(TestTags.DECLINE_BUTTON.name)
        )
        Buttons.FilledPrimary(
            modifier = Modifier.testTag(TestTags.ACCEPT_BUTTON.name),
            text = stringResource(id = R.string.tk_onboarding_analytics_button_primary),
            onClick = acceptTracking,
        )
    }
) {
    ScrollableContent(
        onOpenUserPrivacyPolicyLink = onOpenUserPrivacyPolicyLink
    )
}

@Composable
private fun ScrollableContent(
    onOpenUserPrivacyPolicyLink: () -> Unit
) {
    OnboardingScreenContent(
        title = stringResource(id = R.string.tk_onboarding_analytics_primary),
        subtitle = stringResource(id = R.string.tk_onboarding_analytics_secondary),
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    Buttons.TextLink(
        modifier = Modifier.testTag("privacyPolicy"),
        text = stringResource(id = R.string.tk_onboarding_analytics_tertiary_link_text),
        onClick = onOpenUserPrivacyPolicyLink,
        endIcon = painterResource(id = R.drawable.wallet_ic_chevron),
    )
}

@WalletAllScreenPreview
@Composable
private fun UserPrivacyPolicyScreenContentPreview() {
    WalletTheme {
        UserPrivacyPolicyScreenContent(
            acceptTracking = {},
            declineTracking = {},
            onOpenUserPrivacyPolicyLink = {},
        )
    }
}
