package ch.admin.foitt.wallet.feature.onboarding.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.onboarding.presentation.composables.CollectFocusEvents
import ch.admin.foitt.wallet.feature.onboarding.presentation.composables.OnboardingLoadingScreenContent
import ch.admin.foitt.wallet.platform.biometrics.presentation.BiometricsAvailableImage
import ch.admin.foitt.wallet.platform.biometrics.presentation.BiometricsContent
import ch.admin.foitt.wallet.platform.biometrics.presentation.BiometricsUnavailableImage
import ch.admin.foitt.wallet.platform.composables.AdaptiveBottomButtonBar
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.RequestViewFocusOnResume
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.utils.LocalActivity
import ch.admin.foitt.wallet.platform.utils.OnResumeEventHandler
import ch.admin.foitt.wallet.platform.utils.TestTags
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun OnboardingRegisterBiometricsScreen(viewModel: OnboardingRegisterBiometricsViewModel) {
    OnResumeEventHandler {
        viewModel.refreshScreenState()
    }
    RequestViewFocusOnResume()
    CollectFocusEvents(viewModel.focusEvents)
    val screenState = viewModel.screenState.collectAsStateWithLifecycle().value
    val currentActivity = LocalActivity.current

    AnimatedContent(
        targetState = viewModel.initializationInProgress.collectAsStateWithLifecycle().value,
        label = "loadingFadeIn"
    ) { initializing ->
        if (initializing) {
            OnboardingLoadingScreenContent()
        } else {
            RegisterBiometricsContent(
                onTriggerPrompt = { viewModel.enableBiometrics(currentActivity) },
                onOpenSettings = viewModel::openSettings,
                onSkip = viewModel::declineBiometrics,
                screenState = screenState,
            )
        }
    }
}

@Composable
private fun RegisterBiometricsContent(
    onTriggerPrompt: () -> Unit,
    onOpenSettings: () -> Unit,
    onSkip: () -> Unit,
    screenState: OnboardingRegisterBiometricsScreenState
) {
    val focusManager = LocalFocusManager.current
    val primaryButtonFocusRequester = remember { FocusRequester() }
    val secondaryButtonFocusRequester = remember { FocusRequester() }
    val primaryButtonFocused = remember { mutableStateOf(false) }
    val secondaryButtonFocused = remember { mutableStateOf(false) }
    val focusedOnBackButton = remember { mutableStateOf(true) }
    WalletLayouts.ScrollableColumnWithPicture(
        modifier = Modifier.onPreviewKeyEvent { event ->
            // When back button is focused and tab was pressed
            // we only receive the key up event not the down one.
            when (event.key) {
                Key.Tab if event.type == KeyEventType.KeyUp -> {
                    if (focusedOnBackButton.value) {
                        focusedOnBackButton.value = false
                        primaryButtonFocusRequester.requestFocus()
                    }
                    false
                }
                Key.Tab if event.type == KeyEventType.KeyDown -> {
                    when {
                        primaryButtonFocused.value -> secondaryButtonFocusRequester.requestFocus()
                        secondaryButtonFocused.value -> {
                            focusedOnBackButton.value = true
                            // A little hack to get back to the back button
                            // 1. Clear Focus from decline button
                            focusManager.clearFocus(force = true)
                            // 2. Move Focus in up direction (Back Button)
                            focusManager.moveFocus(FocusDirection.Up)
                        }
                    }
                    true
                }
                else -> {
                    false
                }
            }
        },
        stickyStartContent = {
            BiometricsImage(screenState = screenState)
        },
        stickyBottomContent = {
            BiometricsButtons(
                screenState = screenState,
                onSkip = onSkip,
                onTriggerPrompt = onTriggerPrompt,
                onOpenSettings = onOpenSettings,
                primaryFocusRequester = primaryButtonFocusRequester,
                primaryFocused = primaryButtonFocused,
                secondaryFocusRequester = secondaryButtonFocusRequester,
                secondaryFocused = secondaryButtonFocused
            )
        },
        content = {
            BiometricsBodyContent(
                screenState = screenState,
            )
        }
    )
}

@Composable
private fun BiometricsImage(screenState: OnboardingRegisterBiometricsScreenState) = when (screenState) {
    OnboardingRegisterBiometricsScreenState.Initial -> {}
    OnboardingRegisterBiometricsScreenState.Available -> BiometricsAvailableImage()
    OnboardingRegisterBiometricsScreenState.DisabledForApp,
    OnboardingRegisterBiometricsScreenState.DisabledOnDevice,
    OnboardingRegisterBiometricsScreenState.Error,
    OnboardingRegisterBiometricsScreenState.Lockout -> BiometricsUnavailableImage()
}

@Composable
private fun BiometricsBodyContent(screenState: OnboardingRegisterBiometricsScreenState) {
    when (screenState) {
        OnboardingRegisterBiometricsScreenState.Initial -> {}
        OnboardingRegisterBiometricsScreenState.Available -> BiometricsContent(
            title = R.string.tk_onboarding_biometricsPermission_primary,
            description = R.string.tk_onboarding_biometricsPermission_secondary,
            infoText = R.string.tk_onboarding_biometricsPermission_tertiary,
        )

        OnboardingRegisterBiometricsScreenState.Lockout,
        OnboardingRegisterBiometricsScreenState.Error -> BiometricsContent(
            title = R.string.tk_onboarding_biometricsPermission_primary,
            description = R.string.tk_onboarding_biometricsPermissionLater_secondary,
            infoText = null,
        )

        OnboardingRegisterBiometricsScreenState.DisabledOnDevice -> BiometricsContent(
            title = R.string.tk_onboarding_biometricsPermission_primary,
            description = R.string.tk_onboarding_biometricsPermissionDisabledDevice_primary,
            infoText = R.string.tk_onboarding_biometricsPermissionDisabled_tertiary,
        )

        OnboardingRegisterBiometricsScreenState.DisabledForApp -> BiometricsContent(
            title = R.string.tk_onboarding_biometricsPermissionDisabled_primary,
            description = R.string.tk_onboarding_biometricsPermissionDisabled_secondary,
            infoText = R.string.tk_onboarding_biometricsPermissionDisabled_tertiary,
        )
    }
}

@Composable
private fun BiometricsButtons(
    screenState: OnboardingRegisterBiometricsScreenState,
    onSkip: () -> Unit,
    onTriggerPrompt: () -> Unit,
    onOpenSettings: () -> Unit,
    primaryFocusRequester: FocusRequester,
    secondaryFocusRequester: FocusRequester,
    primaryFocused: MutableState<Boolean>,
    secondaryFocused: MutableState<Boolean>
) {
    val primaryModifier = Modifier
        .focusRequester(primaryFocusRequester)
        .onFocusChanged {
            primaryFocused.value = it.isFocused
        }
    val secondaryModifier = Modifier
        .focusRequester(secondaryFocusRequester)
        .onFocusChanged {
            secondaryFocused.value = it.isFocused
        }

    when (screenState) {
        OnboardingRegisterBiometricsScreenState.Available -> {
            AdaptiveBottomButtonBar(
                buttons = listOf(
                    {
                        Buttons.FilledPrimary(
                            modifier = primaryModifier,
                            text = stringResource(id = R.string.tk_onboarding_biometricsPermission_button_primary),
                            onClick = onTriggerPrompt,
                        )
                    },
                    {
                        Buttons.TonalSecondary(
                            modifier = secondaryModifier,
                            text = stringResource(id = R.string.tk_global_no),
                            onClick = onSkip,
                        )
                    }
                )
            )
        }

        OnboardingRegisterBiometricsScreenState.DisabledForApp -> {
            AdaptiveBottomButtonBar(
                buttons = listOf(
                    {
                        Buttons.FilledPrimary(
                            text = stringResource(id = R.string.tk_onboarding_biometricsPermissionDisabled_button_primary),
                            onClick = onOpenSettings,
                            modifier = primaryModifier
                                .testTag(TestTags.TO_SETTING_BUTTON.name)
                        )
                    },
                    {
                        Buttons.TonalSecondary(
                            text = stringResource(id = R.string.tk_global_no),
                            onClick = onSkip,
                            modifier = secondaryModifier
                                .testTag(TestTags.NO_BIOMETRICS_BUTTON.name)
                        )
                    }
                )
            )
        }

        OnboardingRegisterBiometricsScreenState.DisabledOnDevice -> {
            AdaptiveBottomButtonBar(
                buttons = listOf(
                    {
                        Buttons.FilledPrimary(
                            text = stringResource(id = R.string.tk_onboarding_biometricsPermissionDisabled_button_primary),
                            onClick = onOpenSettings,
                            modifier = primaryModifier
                                .testTag(TestTags.TO_SETTING_BUTTON.name)
                        )
                    },
                    {
                        Buttons.TonalSecondary(
                            text = stringResource(id = R.string.tk_global_no),
                            onClick = onSkip,
                            modifier = secondaryModifier
                                .testTag(TestTags.NO_BIOMETRICS_BUTTON.name)
                        )
                    }
                )
            )
        }

        OnboardingRegisterBiometricsScreenState.Error,
        OnboardingRegisterBiometricsScreenState.Lockout -> {
            AdaptiveBottomButtonBar(
                buttons = listOf(
                    {
                        Buttons.FilledPrimary(
                            text = stringResource(id = R.string.tk_global_continue),
                            onClick = onSkip,
                        )
                    }
                )
            )
        }

        OnboardingRegisterBiometricsScreenState.Initial -> {}
    }
}

private class RegisterBiometricsPreviewParams : PreviewParameterProvider<OnboardingRegisterBiometricsScreenState> {
    override val values = sequenceOf(
        OnboardingRegisterBiometricsScreenState.Available,
        OnboardingRegisterBiometricsScreenState.DisabledOnDevice,
        OnboardingRegisterBiometricsScreenState.Lockout,
        OnboardingRegisterBiometricsScreenState.Error,
        OnboardingRegisterBiometricsScreenState.Initial,
    )
}

@WalletAllScreenPreview
@Composable
private fun RegisterBiometricsPreview(
    @PreviewParameter(RegisterBiometricsPreviewParams::class) previewParams: OnboardingRegisterBiometricsScreenState
) {
    WalletTheme {
        RegisterBiometricsContent(
            onTriggerPrompt = {},
            onSkip = {},
            onOpenSettings = {},
            screenState = previewParams,
        )
    }
}
