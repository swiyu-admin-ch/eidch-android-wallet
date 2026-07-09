package ch.admin.foitt.wallet.feature.onboarding.presentation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.onboarding.presentation.composables.OnboardingScreenContent
import ch.admin.foitt.wallet.feature.onboarding.presentation.composables.OnboardingSwipeableScreen
import ch.admin.foitt.wallet.platform.composables.AdaptiveBottomButtonBar
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.ScreenMainImage
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.utils.TestTags
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun OnboardingPrivacyPolicyScreen(viewModel: OnboardingPrivacyPolicyViewModel) {
    OnboardingSwipeableScreen(
        onSwipeForward = {},
        onSwipeBackWard = viewModel::onBack,
        focusEvents = viewModel.focusEvents
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
) {
    val upArrowFocusRequester = remember { FocusRequester() }
    val acceptButtonFocusRequester = remember { FocusRequester() }
    val declineButtonFocusRequester = remember { FocusRequester() }
    var isDeclineButtonFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    WalletLayouts.ScrollableColumnWithPicture(
        stickyStartContent = {
            ScreenMainImage(
                modifier = Modifier.testTag(TestTags.USER_PRIVACY_ICON.name),
                iconRes = R.drawable.wallet_ic_verify_cross,
                backgroundRes = R.drawable.wallet_background_gradient_06,
            )
        },
        stickyBottomContent = {
            AdaptiveBottomButtonBar(
                buttons = listOf(
                    {
                        Buttons.FilledPrimary(
                            modifier = Modifier
                                .testTag(TestTags.ACCEPT_BUTTON.name)
                                .focusRequester(acceptButtonFocusRequester)
                                .focusProperties {
                                    up = upArrowFocusRequester
                                    next = declineButtonFocusRequester
                                },
                            text = stringResource(id = R.string.tk_onboarding_analytics_button_primary),
                            onClick = acceptTracking,
                        )
                    },
                    {
                        Buttons.TonalSecondary(
                            text = stringResource(id = R.string.tk_onboarding_analytics_button_secondary),
                            onClick = declineTracking,
                            modifier = Modifier
                                .semantics {
                                    traversalIndex = 1f
                                }
                                .testTag(TestTags.DECLINE_BUTTON.name)
                                .focusRequester(declineButtonFocusRequester)
                                .focusProperties {
                                    up = upArrowFocusRequester
                                }
                                .onFocusChanged {
                                    isDeclineButtonFocused = it.isFocused
                                }
                                .onPreviewKeyEvent { event ->
                                    if (isDeclineButtonFocused && event.key == Key.Tab && event.type == KeyEventType.KeyDown) {
                                        // A little hack to get back to the back button
                                        // 1. Clear Focus from decline button
                                        focusManager.clearFocus(force = true)
                                        // 2. Move Focus to next (Which will be the title label)
                                        focusManager.moveFocus(FocusDirection.Next)
                                        // 3. Move Focus in up direction (Back Button above title label)
                                        focusManager.moveFocus(FocusDirection.Up)
                                        true
                                    } else {
                                        false
                                    }
                                }
                        )
                    }
                )
            )
        }
    ) {
        ScrollableContent(
            onOpenUserPrivacyPolicyLink = onOpenUserPrivacyPolicyLink,
            upArrowFocusRequester = upArrowFocusRequester,
            acceptButtonFocusRequester = acceptButtonFocusRequester
        )
    }
}

@Composable
private fun ScrollableContent(
    onOpenUserPrivacyPolicyLink: () -> Unit,
    upArrowFocusRequester: FocusRequester,
    acceptButtonFocusRequester: FocusRequester
) {
    OnboardingScreenContent(
        title = stringResource(id = R.string.tk_onboarding_analytics_primary),
        subtitle = stringResource(id = R.string.tk_onboarding_analytics_secondary),
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    Buttons.TextLink(
        modifier = Modifier
            .testTag("privacyPolicy")
            .focusRequester(upArrowFocusRequester)
            .focusProperties {
                down = acceptButtonFocusRequester
                next = acceptButtonFocusRequester
            },
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
