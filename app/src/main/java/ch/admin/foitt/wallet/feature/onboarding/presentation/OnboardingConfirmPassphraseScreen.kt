package ch.admin.foitt.wallet.feature.onboarding.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.onboarding.presentation.composables.OnboardingLoadingScreenContent
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.PassphraseValidationErrorToastFixed
import ch.admin.foitt.wallet.platform.composables.presentation.WindowWidthClass
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.composables.presentation.windowWidthClass
import ch.admin.foitt.wallet.platform.passphraseInput.domain.model.PassphraseInputFieldState
import ch.admin.foitt.wallet.platform.passphraseInput.presentation.PassphraseInputComponent
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.scaffold.presentation.FullscreenGradient
import ch.admin.foitt.wallet.platform.utils.TestTags
import ch.admin.foitt.wallet.platform.utils.isScreenReaderOn
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTextFieldColors
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun OnboardingConfirmPassphraseScreen(
    viewModel: OnboardingConfirmPassphraseViewModel,
) {
    OnboardingConfirmPassphraseScreenContent(
        textFieldValue = viewModel.textFieldValue.collectAsStateWithLifecycle().value,
        passphraseInputFieldState = viewModel.passphraseInputFieldState.collectAsStateWithLifecycle().value,
        showSupportText = viewModel.showSupportText.collectAsStateWithLifecycle().value,
        confirmationAttemptsLeft = viewModel.remainingConfirmationAttempts.collectAsStateWithLifecycle().value,
        showPassphraseErrorToast = viewModel.showPassphraseErrorToast.collectAsStateWithLifecycle().value,
        isPassphraseValid = viewModel.isPassphraseValid.collectAsStateWithLifecycle().value,
        isInitializing = viewModel.isInitializing.collectAsStateWithLifecycle().value,
        onTextFieldValueChange = viewModel::onTextFieldValueChange,
        onCheckPassphrase = viewModel::onCheckPassphrase,
        onClosePassphraseError = viewModel::onClosePassphraseError,
    )
}

@Composable
private fun OnboardingConfirmPassphraseScreenContent(
    textFieldValue: TextFieldValue,
    confirmationAttemptsLeft: Int,
    passphraseInputFieldState: PassphraseInputFieldState,
    showSupportText: Boolean,
    showPassphraseErrorToast: Boolean,
    isPassphraseValid: Boolean,
    isInitializing: Boolean,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onCheckPassphrase: () -> Unit,
    onClosePassphraseError: () -> Unit,
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
                confirmationAttemptsLeft = confirmationAttemptsLeft,
                isPassphraseValid = isPassphraseValid,
                showSupportText = showSupportText,
                showPassphraseErrorToast = showPassphraseErrorToast,
                onTextFieldValueChange = onTextFieldValueChange,
                onCheckPassphrase = onCheckPassphrase,
                onClosePassphraseError = onClosePassphraseError,
            )
        }
    }
}

@Composable
private fun OnboardingConfirmPassphraseContent(
    textFieldValue: TextFieldValue,
    passphraseInputFieldState: PassphraseInputFieldState,
    confirmationAttemptsLeft: Int,
    isPassphraseValid: Boolean,
    showSupportText: Boolean,
    showPassphraseErrorToast: Boolean,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onCheckPassphrase: () -> Unit,
    onClosePassphraseError: () -> Unit,
) {
    FullscreenGradient()

    when (currentWindowAdaptiveInfo().windowWidthClass()) {
        WindowWidthClass.COMPACT -> WalletLayouts.CompactContainerFloatingBottom(
            verticalArrangement = Arrangement.Top,
            shouldScrollUnderTopBar = false,
            content = {
                CompactContent(
                    textFieldValue = textFieldValue,
                    passphraseInputFieldState = passphraseInputFieldState,
                    confirmationAttemptsLeft = confirmationAttemptsLeft,
                    showSupportText = showSupportText,
                    onTextFieldValueChange = onTextFieldValueChange,
                    onCheckPassphrase = onCheckPassphrase
                )
            },
            auxiliaryContent = {
                AuxiliaryContent(
                    compactLayout = true,
                    passphraseInputFieldState = passphraseInputFieldState,
                    showPassphraseErrorToast = showPassphraseErrorToast,
                    onClosePassphraseError = onClosePassphraseError,
                )
            },
            stickyBottomHorizontalAlignment = Alignment.End,
            stickyBottomContent = {
                BottomButton(
                    isEnabled = isPassphraseValid,
                    onCheckPassphrase = onCheckPassphrase,
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
                    confirmationAttemptsLeft = confirmationAttemptsLeft,
                    isPassphraseValid = isPassphraseValid,
                    showSupportText = showSupportText,
                    onTextFieldValueChange = onTextFieldValueChange,
                    onCheckPassphrase = onCheckPassphrase
                )
            },
            auxiliaryContent = {
                AuxiliaryContent(
                    compactLayout = false,
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
    confirmationAttemptsLeft: Int,
    showSupportText: Boolean,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onCheckPassphrase: () -> Unit,
) {
    Spacer(modifier = Modifier.height(Sizes.s12))
    PassphraseInputComponent(
        modifier = Modifier.fillMaxWidth(),
        passphraseInputFieldState = passphraseInputFieldState,
        textFieldValue = textFieldValue,
        colors = WalletTextFieldColors.textFieldColorsFixed(),
        placeholder = {
            Placeholder()
        },
        supportingText = {
            if (showSupportText) {
                SupportingText(
                    confirmationAttemptsLeft = confirmationAttemptsLeft,
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
    confirmationAttemptsLeft: Int,
    showSupportText: Boolean,
    isPassphraseValid: Boolean,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onCheckPassphrase: () -> Unit,
) {
    Spacer(modifier = Modifier.height(Sizes.s04))
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        PassphraseInputComponent(
            modifier = Modifier.weight(1f),
            passphraseInputFieldState = passphraseInputFieldState,
            textFieldValue = textFieldValue,
            colors = WalletTextFieldColors.textFieldColorsFixed(),
            placeholder = {
                Placeholder()
            },
            supportingText = {
                if (showSupportText) {
                    SupportingText(
                        confirmationAttemptsLeft = confirmationAttemptsLeft,
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
private fun Placeholder() = WalletTexts.BodyLarge(
    text = stringResource(R.string.tk_onboarding_passwordConfirmation_input_placeholder),
    color = WalletTheme.colorScheme.onSurfaceVariantFixed
)

@Composable
private fun SupportingText(
    confirmationAttemptsLeft: Int,
) = WalletTexts.BodySmall(
    modifier = Modifier,
    text = pluralStringResource(
        R.plurals.tk_onboarding_passwordConfirmation_input_error_numberOfTriesLeft,
        confirmationAttemptsLeft,
        confirmationAttemptsLeft
    ),
    color = WalletTheme.colorScheme.onGradientFixed
)

@Composable
private fun AuxiliaryContent(
    compactLayout: Boolean,
    passphraseInputFieldState: PassphraseInputFieldState,
    showPassphraseErrorToast: Boolean,
    onClosePassphraseError: () -> Unit,
) {
    if (passphraseInputFieldState == PassphraseInputFieldState.Error && showPassphraseErrorToast) {
        PassphraseValidationErrorToastFixed(
            modifier = Modifier
                .padding(start = Sizes.s08, end = Sizes.s08, bottom = if (compactLayout) Sizes.s06 else Sizes.s04),
            text = R.string.tk_onboarding_nopasswordmismatch_notification,
            onIconEnd = onClosePassphraseError,
            shouldRequestFocus = LocalContext.current.isScreenReaderOn()
        )
    }
}

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

@WalletAllScreenPreview
@Composable
private fun OnboardingConfirmPassphraseScreenPreview() {
    WalletTheme {
        OnboardingConfirmPassphraseScreenContent(
            textFieldValue = TextFieldValue("abc123"),
            confirmationAttemptsLeft = 4,
            passphraseInputFieldState = PassphraseInputFieldState.Error,
            showSupportText = true,
            showPassphraseErrorToast = true,
            isPassphraseValid = true,
            isInitializing = false,
            onTextFieldValueChange = {},
            onCheckPassphrase = {},
            onClosePassphraseError = {},
        )
    }
}
