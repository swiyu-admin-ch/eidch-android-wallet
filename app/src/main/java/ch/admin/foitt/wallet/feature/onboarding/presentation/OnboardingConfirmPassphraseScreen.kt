package ch.admin.foitt.wallet.feature.onboarding.presentation

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.onboarding.presentation.composables.CollectFocusEvents
import ch.admin.foitt.wallet.feature.onboarding.presentation.composables.OnboardingLoadingScreenContent
import ch.admin.foitt.wallet.platform.composables.AdaptiveButtonContainer
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.PassphraseValidationErrorToastFixed
import ch.admin.foitt.wallet.platform.composables.presentation.RequestViewFocusOnResume
import ch.admin.foitt.wallet.platform.composables.presentation.WindowWidthClass
import ch.admin.foitt.wallet.platform.composables.presentation.bottomSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.composables.presentation.windowWidthClass
import ch.admin.foitt.wallet.platform.passphraseInput.domain.model.PassphraseInputFieldState
import ch.admin.foitt.wallet.platform.passphraseInput.presentation.PassphraseInputComponent
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.scaffold.presentation.FullscreenGradient
import ch.admin.foitt.wallet.platform.utils.TestTags
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTextFieldColors
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun OnboardingConfirmPassphraseScreen(
    viewModel: OnboardingConfirmPassphraseViewModel,
) {
    RequestViewFocusOnResume()
    val passwordFocusRequester = remember { FocusRequester() }
    CollectFocusEvents(viewModel.focusEvents) {
        passwordFocusRequester.requestFocus()
    }
    OnboardingConfirmPassphraseScreenContent(
        textFieldValue = viewModel.textFieldValue.collectAsStateWithLifecycle().value,
        passphraseInputFieldState = viewModel.passphraseInputFieldState.collectAsStateWithLifecycle().value,
        attemptsLeft = viewModel.remainingConfirmationAttempts.collectAsStateWithLifecycle().value,
        showSupportText = viewModel.showSupportText.collectAsStateWithLifecycle().value,
        showPassphraseErrorToast = viewModel.showPassphraseErrorToast.collectAsStateWithLifecycle().value,
        isPassphraseValid = viewModel.isPassphraseValid.collectAsStateWithLifecycle().value,
        isInitializing = viewModel.isInitializing.collectAsStateWithLifecycle().value,
        onTextFieldValueChange = viewModel::onTextFieldValueChange,
        onCheckPassphrase = viewModel::onCheckPassphrase,
        onClosePassphraseError = viewModel::onClosePassphraseError,
        passwordFocusRequester = passwordFocusRequester
    )
}

@Composable
private fun OnboardingConfirmPassphraseScreenContent(
    textFieldValue: TextFieldValue,
    passphraseInputFieldState: PassphraseInputFieldState,
    attemptsLeft: Int,
    showSupportText: Boolean,
    showPassphraseErrorToast: Boolean,
    isPassphraseValid: Boolean,
    isInitializing: Boolean,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onCheckPassphrase: () -> Unit,
    onClosePassphraseError: () -> Unit,
    passwordFocusRequester: FocusRequester
) {
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(isInitializing) {
        if (isInitializing) {
            keyboard?.hide()
        }
    }

    AnimatedContent(targetState = isInitializing, label = "loadingFadeIn") { initializing ->
        if (initializing) {
            OnboardingLoadingScreenContent()
        } else {
            OnboardingConfirmPassphraseContent(
                textFieldValue = textFieldValue,
                passphraseInputFieldState = passphraseInputFieldState,
                attemptsLeft = attemptsLeft,
                isPassphraseValid = isPassphraseValid,
                showSupportText = showSupportText,
                showPassphraseErrorToast = showPassphraseErrorToast,
                onTextFieldValueChange = onTextFieldValueChange,
                onCheckPassphrase = onCheckPassphrase,
                onClosePassphraseError = onClosePassphraseError,
                passwordFocusRequester = passwordFocusRequester
            )
        }
    }
}

@Composable
private fun OnboardingConfirmPassphraseContent(
    textFieldValue: TextFieldValue,
    passphraseInputFieldState: PassphraseInputFieldState,
    attemptsLeft: Int,
    isPassphraseValid: Boolean,
    showSupportText: Boolean,
    showPassphraseErrorToast: Boolean,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onCheckPassphrase: () -> Unit,
    onClosePassphraseError: () -> Unit,
    passwordFocusRequester: FocusRequester
) {
    FullscreenGradient()

    when (currentWindowAdaptiveInfoV2().windowWidthClass()) {
        WindowWidthClass.COMPACT -> WalletLayouts.CompactContainerFloatingBottom(
            verticalArrangement = Arrangement.Top,
            shouldScrollUnderTopBar = false,
            content = {
                CompactContent(
                    textFieldValue = textFieldValue,
                    passphraseInputFieldState = passphraseInputFieldState,
                    attemptsLeft = attemptsLeft,
                    showSupportText = showSupportText,
                    onTextFieldValueChange = onTextFieldValueChange,
                    onCheckPassphrase = onCheckPassphrase,
                    passwordFocusRequester = passwordFocusRequester
                )
            },
            auxiliaryContent = {
                AuxiliaryContent(
                    passphraseInputFieldState = passphraseInputFieldState,
                    showPassphraseErrorToast = showPassphraseErrorToast,
                    onClosePassphraseError = onClosePassphraseError,
                )
            },
            stickyBottomContent = {
                AdaptiveButtonContainer(
                    buttons = listOf(
                        {
                            BottomButton(
                                isEnabled = isPassphraseValid,
                                onCheckPassphrase = onCheckPassphrase,
                            )
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .bottomSafeDrawing()
                        .padding(Sizes.s04)
                )
            },
        )

        else -> WalletLayouts.LargeContainerFloatingBottom(
            verticalArrangement = Arrangement.Top,
            shouldScrollUnderTopBar = false,
            content = {
                LargeContent(
                    textFieldValue = textFieldValue,
                    passphraseInputFieldState = passphraseInputFieldState,
                    attemptsLeft = attemptsLeft,
                    isPassphraseValid = isPassphraseValid,
                    showSupportText = showSupportText,
                    onTextFieldValueChange = onTextFieldValueChange,
                    onCheckPassphrase = onCheckPassphrase,
                    passwordFocusRequester = passwordFocusRequester
                )
            },
            auxiliaryContent = {
                AuxiliaryContent(
                    passphraseInputFieldState = passphraseInputFieldState,
                    showPassphraseErrorToast = showPassphraseErrorToast,
                    onClosePassphraseError = onClosePassphraseError,
                )
            },
        )
    }
}

@Composable
private fun CompactContent(
    textFieldValue: TextFieldValue,
    passphraseInputFieldState: PassphraseInputFieldState,
    attemptsLeft: Int,
    showSupportText: Boolean,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onCheckPassphrase: () -> Unit,
    passwordFocusRequester: FocusRequester
) {
    Spacer(modifier = Modifier.height(Sizes.s12))
    PassphraseInputComponent(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(passwordFocusRequester),
        passphraseInputFieldState = passphraseInputFieldState,
        errorMessage = errorMessage(
            showSupportText = showSupportText,
            attemptsLeft = attemptsLeft,
        ),
        textFieldValue = textFieldValue,
        colors = WalletTextFieldColors.textFieldColorsFixed(),
        placeholder = {
            Placeholder()
        },
        supportingText = {
            if (showSupportText) {
                SupportingText(
                    attemptsLeft = attemptsLeft,
                )
            }
        },
        keyboardImeAction = ImeAction.Next,
        onKeyboardAction = onCheckPassphrase,
        onTextFieldValueChange = onTextFieldValueChange,
        onAnimationFinished = {},
    )
}

@Composable
private fun LargeContent(
    textFieldValue: TextFieldValue,
    passphraseInputFieldState: PassphraseInputFieldState,
    attemptsLeft: Int,
    showSupportText: Boolean,
    isPassphraseValid: Boolean,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onCheckPassphrase: () -> Unit,
    passwordFocusRequester: FocusRequester
) {
    Spacer(modifier = Modifier.height(Sizes.s04))
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        PassphraseInputComponent(
            modifier = Modifier
                .weight(1f)
                .focusRequester(passwordFocusRequester),
            passphraseInputFieldState = passphraseInputFieldState,
            errorMessage = errorMessage(
                showSupportText = showSupportText,
                attemptsLeft = attemptsLeft,
            ),
            textFieldValue = textFieldValue,
            colors = WalletTextFieldColors.textFieldColorsFixed(),
            placeholder = {
                Placeholder()
            },
            supportingText = {
                if (showSupportText) {
                    SupportingText(
                        attemptsLeft = attemptsLeft,
                    )
                }
            },
            keyboardImeAction = ImeAction.Next,
            onKeyboardAction = onCheckPassphrase,
            onTextFieldValueChange = onTextFieldValueChange,
            onAnimationFinished = {},
        )
        Spacer(modifier = Modifier.width(Sizes.s08))
        BottomButton(
            isEnabled = isPassphraseValid,
            onCheckPassphrase = onCheckPassphrase
        )
    }
}

@Composable
private fun AuxiliaryContent(
    passphraseInputFieldState: PassphraseInputFieldState,
    showPassphraseErrorToast: Boolean,
    onClosePassphraseError: () -> Unit,
) {
    if (passphraseInputFieldState is PassphraseInputFieldState.Error && showPassphraseErrorToast) {
        PassphraseValidationErrorToastFixed(
            modifier = Modifier
                .padding(start = Sizes.s08, end = Sizes.s08, bottom = Sizes.s06),
            text = R.string.tk_onboarding_nopasswordmismatch_notification,
            errorCode = passphraseInputFieldState.errorCode,
            onIconEnd = onClosePassphraseError,
        )
    }
}

@Composable
private fun Placeholder() = WalletTexts.BodyLarge(
    text = stringResource(R.string.tk_onboarding_passwordConfirmation_input_placeholder),
    color = WalletTheme.colorScheme.onSurfaceVariantFixed
)

@Composable
private fun attemptsLeftText(
    attemptsLeft: Int,
): String = pluralStringResource(
    R.plurals.tk_onboarding_passwordConfirmation_input_error_numberOfTriesLeft,
    attemptsLeft,
    attemptsLeft
)

@Composable
private fun errorMessage(
    showSupportText: Boolean,
    attemptsLeft: Int,
): String {
    val mismatch = stringResource(R.string.tk_onboarding_nopasswordmismatch_notification)
    return when {
        showSupportText -> "$mismatch ${attemptsLeftText(attemptsLeft)}"
        else -> mismatch
    }
}

@Composable
private fun SupportingText(
    attemptsLeft: Int,
) = WalletTexts.BodySmall(
    text = attemptsLeftText(attemptsLeft),
    color = WalletTheme.colorScheme.onGradientFixed
)

@Composable
private fun BottomButton(
    isEnabled: Boolean,
    onCheckPassphrase: () -> Unit,
) = Buttons.FilledPrimaryFixed(
    modifier = Modifier.testTag(TestTags.CONTINUE_BUTTON.name),
    enabled = isEnabled,
    text = stringResource(R.string.tk_global_continue),
    onClick = onCheckPassphrase
)

@SuppressLint("RememberInComposition")
@WalletAllScreenPreview
@Composable
private fun OnboardingConfirmPassphraseScreenPreview() {
    WalletTheme {
        OnboardingConfirmPassphraseScreenContent(
            textFieldValue = TextFieldValue("abc123"),
            attemptsLeft = 4,
            passphraseInputFieldState = PassphraseInputFieldState.Error("XY123-1234"),
            showSupportText = true,
            showPassphraseErrorToast = true,
            isPassphraseValid = true,
            isInitializing = false,
            onTextFieldValueChange = {},
            onCheckPassphrase = {},
            onClosePassphraseError = {},
            passwordFocusRequester = FocusRequester()
        )
    }
}
