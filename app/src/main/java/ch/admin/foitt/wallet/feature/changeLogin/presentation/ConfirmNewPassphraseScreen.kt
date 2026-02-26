package ch.admin.foitt.wallet.feature.changeLogin.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.presentation.WindowWidthClass
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.composables.presentation.windowWidthClass
import ch.admin.foitt.wallet.platform.passphraseInput.domain.model.PassphraseInputFieldState
import ch.admin.foitt.wallet.platform.passphraseInput.presentation.PassphraseInputComponent
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.utils.OnResumeEventHandler
import ch.admin.foitt.wallet.platform.utils.TestTags
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTextFieldColors
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun ConfirmNewPassphraseScreen(viewModel: ConfirmNewPassphraseViewModel) {
    OnResumeEventHandler {
        viewModel.checkRemainingConfirmationAttempts()
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val isLoading = viewModel.isLoading.collectAsStateWithLifecycle().value

    LaunchedEffect(isLoading) {
        when (isLoading) {
            true -> keyboardController?.hide()
            false -> keyboardController?.show()
        }
    }

    ConfirmNewPassphraseScreenContent(
        textFieldValue = viewModel.textFieldValue.collectAsStateWithLifecycle().value,
        passphraseInputFieldState = viewModel.passphraseInputFieldState.collectAsStateWithLifecycle().value,
        isPassphraseValid = viewModel.isPassphraseValid.collectAsStateWithLifecycle().value,
        hideSupportText = viewModel.hideSupportText.collectAsStateWithLifecycle().value,
        remainingConfirmationAttempts = viewModel.remainingConfirmationAttempts.collectAsStateWithLifecycle().value,
        isLoading = isLoading,
        onTextFieldValueChange = viewModel::onTextFieldValueChange,
        onCheckPassphrase = viewModel::onCheckPassphrase,
    )
}

@Composable
private fun ConfirmNewPassphraseScreenContent(
    textFieldValue: TextFieldValue,
    passphraseInputFieldState: PassphraseInputFieldState,
    isPassphraseValid: Boolean,
    hideSupportText: Boolean,
    remainingConfirmationAttempts: Int,
    isLoading: Boolean,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onCheckPassphrase: () -> Unit,
) {
    when (currentWindowAdaptiveInfo().windowWidthClass()) {
        WindowWidthClass.COMPACT -> WalletLayouts.CompactContainerFloatingBottom(
            modifier = Modifier.background(WalletTheme.colorScheme.surfaceContainerLow),
            shouldScrollUnderTopBar = false,
            verticalArrangement = Arrangement.Top,
            content = {
                CompactContent(
                    textFieldValue = textFieldValue,
                    passphraseInputFieldState = passphraseInputFieldState,
                    hideSupportText = hideSupportText,
                    remainingConfirmationAttempts = remainingConfirmationAttempts,
                    onTextFieldValueChange = onTextFieldValueChange,
                    onCheckPassphrase = onCheckPassphrase
                )
            },
            stickyBottomHorizontalAlignment = Alignment.End,
            stickyBottomContent = {
                BottomButton(
                    enabled = isPassphraseValid,
                    onCheckPassphrase = onCheckPassphrase,
                )
            },
        )

        else -> WalletLayouts.LargeContainerFloatingBottom(
            modifier = Modifier.background(WalletTheme.colorScheme.surfaceContainerLow),
            shouldScrollUnderTopBar = false,
            verticalArrangement = Arrangement.Top,
            content = {
                LargeContent(
                    textFieldValue = textFieldValue,
                    passphraseInputFieldState = passphraseInputFieldState,
                    isPassphraseValid = isPassphraseValid,
                    hideSupportText = hideSupportText,
                    remainingConfirmationAttempts = remainingConfirmationAttempts,
                    onTextFieldValueChange = onTextFieldValueChange,
                    onCheckPassphrase = onCheckPassphrase
                )
            },
        )
    }
    LoadingOverlay(isLoading)
}

@Composable
private fun CompactContent(
    textFieldValue: TextFieldValue,
    passphraseInputFieldState: PassphraseInputFieldState,
    hideSupportText: Boolean,
    remainingConfirmationAttempts: Int,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onCheckPassphrase: () -> Unit,
) {
    Spacer(modifier = Modifier.height(Sizes.s04))
    PassphraseInputComponent(
        modifier = Modifier.fillMaxWidth(),
        colors = WalletTextFieldColors.textFieldColors(),
        passphraseInputFieldState = passphraseInputFieldState,
        textFieldValue = textFieldValue,
        label = {
            Label(
                passphraseInputFieldState = passphraseInputFieldState,
            )
        },
        supportingText = {
            if (!hideSupportText) {
                SupportingText(
                    remainingConfirmationAttempts = remainingConfirmationAttempts,
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
    isPassphraseValid: Boolean,
    hideSupportText: Boolean,
    remainingConfirmationAttempts: Int,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onCheckPassphrase: () -> Unit,
) {
    Spacer(modifier = Modifier.height(Sizes.s04))
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        PassphraseInputComponent(
            modifier = Modifier.weight(1f),
            colors = WalletTextFieldColors.textFieldColors(),
            passphraseInputFieldState = passphraseInputFieldState,
            textFieldValue = textFieldValue,
            label = {
                Label(
                    passphraseInputFieldState = passphraseInputFieldState,
                )
            },
            supportingText = {
                if (!hideSupportText) {
                    SupportingText(
                        remainingConfirmationAttempts = remainingConfirmationAttempts,
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
            enabled = isPassphraseValid,
            onCheckPassphrase = onCheckPassphrase
        )
    }
}

@Composable
private fun Label(
    passphraseInputFieldState: PassphraseInputFieldState,
) = WalletTexts.BodyLarge(
    text = stringResource(R.string.tk_changepassword_step3_note1),
    color = if (passphraseInputFieldState == PassphraseInputFieldState.Error) {
        WalletTheme.colorScheme.error
    } else {
        WalletTheme.colorScheme.onSurfaceVariant
    }
)

@Composable
private fun SupportingText(
    remainingConfirmationAttempts: Int,
) = WalletTexts.BodySmall(
    text = pluralStringResource(
        R.plurals.tk_changepassword_error1_android_note2,
        remainingConfirmationAttempts,
        remainingConfirmationAttempts
    ),
    color = WalletTheme.colorScheme.error
)

@Composable
private fun BottomButton(
    enabled: Boolean,
    onCheckPassphrase: () -> Unit
) = Buttons.FilledPrimary(
    modifier = Modifier.testTag(TestTags.CONTINUE_BUTTON.name),
    text = stringResource(R.string.tk_global_continue),
    enabled = enabled,
    onClick = onCheckPassphrase
)

@WalletAllScreenPreview
@Composable
private fun ConfirmNewPassphraseScreenPreview() {
    WalletTheme {
        ConfirmNewPassphraseScreenContent(
            textFieldValue = TextFieldValue("abc123"),
            passphraseInputFieldState = PassphraseInputFieldState.Error,
            isPassphraseValid = true,
            hideSupportText = false,
            remainingConfirmationAttempts = 4,
            isLoading = false,
            onTextFieldValueChange = {},
            onCheckPassphrase = {},
        )
    }
}
