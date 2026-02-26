package ch.admin.foitt.wallet.feature.onboarding.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.WindowWidthClass
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
fun OnboardingSetupPassphraseScreen(
    viewModel: OnboardingSetupPassphraseViewModel,
) {
    OnboardingPassphraseScreenContent(
        textFieldValue = viewModel.textFieldValue.collectAsStateWithLifecycle().value,
        passphraseInputFieldState = viewModel.passphraseInputFieldState.collectAsStateWithLifecycle().value,
        isPassphraseValid = viewModel.isPassphraseValid.collectAsStateWithLifecycle().value,
        onTextFieldValueChange = viewModel::onTextFieldValueChange,
        onCheckPassphrase = viewModel::onCheckPassphrase,
    )
}

@Composable
private fun OnboardingPassphraseScreenContent(
    textFieldValue: TextFieldValue,
    passphraseInputFieldState: PassphraseInputFieldState,
    isPassphraseValid: Boolean,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onCheckPassphrase: () -> Unit,
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
                    onTextFieldValueChange = onTextFieldValueChange,
                    onCheckPassphrase = onCheckPassphrase
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
                    isPassphraseValid = isPassphraseValid,
                    onTextFieldValueChange = onTextFieldValueChange,
                    onCheckPassphrase = onCheckPassphrase
                )
            },
        )
    }
}

@Composable
private fun CompactContent(
    textFieldValue: TextFieldValue,
    passphraseInputFieldState: PassphraseInputFieldState,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onCheckPassphrase: () -> Unit,
) {
    Spacer(modifier = Modifier.height(Sizes.s12))
    PassphraseInputComponent(
        modifier = Modifier.fillMaxWidth(),
        passphraseInputFieldState = passphraseInputFieldState,
        textFieldValue = textFieldValue,
        colors = WalletTextFieldColors.textFieldColorsFixed(),
        placeholder = { Placeholder() },
        supportingText = { SupportingText() },
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
            placeholder = { Placeholder() },
            supportingText = { SupportingText() },
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
    text = stringResource(R.string.tk_onboarding_password_input_placeholder),
    color = WalletTheme.colorScheme.onSurfaceVariantFixed
)

@Composable
private fun SupportingText() = WalletTexts.BodySmall(
    modifier = Modifier,
    text = stringResource(R.string.tk_onboarding_password_input_subtitle),
    color = WalletTheme.colorScheme.onGradientFixed
)

@Composable
private fun BottomButton(
    isEnabled: Boolean,
    onCheckPassphrase: () -> Unit,
) = Buttons.FilledPrimaryFixed(
    enabled = isEnabled,
    modifier = Modifier.testTag(TestTags.CONTINUE_BUTTON.name),
    text = stringResource(R.string.tk_global_continue),
    onClick = onCheckPassphrase
)

@WalletAllScreenPreview
@Composable
private fun OnboardingPassphraseScreenPreview() {
    WalletTheme {
        OnboardingPassphraseScreenContent(
            textFieldValue = TextFieldValue("abc123"),
            passphraseInputFieldState = PassphraseInputFieldState.Error,
            onTextFieldValueChange = {},
            isPassphraseValid = true,
            onCheckPassphrase = {},
        )
    }
}
