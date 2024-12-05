package ch.admin.foitt.wallet.feature.changeLogin.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.TextFieldDefaults
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowWidthSizeClass
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.navArgs.domain.model.ConfirmNewPassphraseNavArg
import ch.admin.foitt.wallet.platform.passphraseInput.domain.model.PassphraseInputFieldState
import ch.admin.foitt.wallet.platform.passphraseInput.presentation.PassphraseInputComponent
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.utils.OnResumeEventHandler
import ch.admin.foitt.wallet.platform.utils.TestTags
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.annotation.Destination

@Destination(
    navArgsDelegate = ConfirmNewPassphraseNavArg::class,
)
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
        passphrase = viewModel.passphrase.collectAsStateWithLifecycle().value,
        passphraseInputFieldState = viewModel.passphraseInputFieldState.collectAsStateWithLifecycle().value,
        isNextButtonEnabled = viewModel.isNextButtonEnabled.collectAsStateWithLifecycle().value,
        hideSupportText = viewModel.hideSupportText.collectAsStateWithLifecycle().value,
        remainingConfirmationAttempts = viewModel.remainingConfirmationAttempts.collectAsStateWithLifecycle().value,
        isLoading = isLoading,
        onUpdatePassphrase = viewModel::onUpdatePassphrase,
        onCheckPassphrase = viewModel::onCheckPassphrase,
    )
}

@Composable
private fun ConfirmNewPassphraseScreenContent(
    passphrase: String,
    passphraseInputFieldState: PassphraseInputFieldState,
    isNextButtonEnabled: Boolean,
    hideSupportText: Boolean,
    remainingConfirmationAttempts: Int,
    isLoading: Boolean,
    onUpdatePassphrase: (String) -> Unit,
    onCheckPassphrase: () -> Unit,
) {
    when (currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass) {
        WindowWidthSizeClass.COMPACT -> WalletLayouts.CompactContainerFloatingBottom(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            verticalArrangement = Arrangement.Top,
            content = {
                CompactContent(
                    passphrase = passphrase,
                    passphraseInputFieldState = passphraseInputFieldState,
                    hideSupportText = hideSupportText,
                    remainingConfirmationAttempts = remainingConfirmationAttempts,
                    onUpdatePassphrase = onUpdatePassphrase,
                    onCheckPassphrase = onCheckPassphrase
                )
            },
            stickyBottomHorizontalAlignment = Alignment.End,
            stickyBottomContent = {
                BottomButton(
                    enabled = isNextButtonEnabled,
                    onCheckPassphrase = onCheckPassphrase,
                )
            },
        )

        else -> WalletLayouts.LargeContainerFloatingBottom(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            useStatusBarPadding = false,
            verticalArrangement = Arrangement.Top,
            content = {
                LargeContent(
                    passphrase = passphrase,
                    passphraseInputFieldState = passphraseInputFieldState,
                    isNextButtonEnabled = isNextButtonEnabled,
                    hideSupportText = hideSupportText,
                    remainingConfirmationAttempts = remainingConfirmationAttempts,
                    onUpdatePassphrase = onUpdatePassphrase,
                    onCheckPassphrase = onCheckPassphrase
                )
            },
        )
    }
    LoadingOverlay(isLoading)
}

@Composable
private fun CompactContent(
    passphrase: String,
    passphraseInputFieldState: PassphraseInputFieldState,
    hideSupportText: Boolean,
    remainingConfirmationAttempts: Int,
    onUpdatePassphrase: (String) -> Unit,
    onCheckPassphrase: () -> Unit,
) {
    Spacer(modifier = Modifier.height(Sizes.s04))
    PassphraseInputComponent(
        modifier = Modifier.fillMaxWidth(),
        colors = textFieldColors(),
        passphraseInputFieldState = passphraseInputFieldState,
        passphrase = passphrase,
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
        onPassphraseChange = onUpdatePassphrase,
        onAnimationFinished = {},
    )
}

@Composable
private fun LargeContent(
    passphrase: String,
    passphraseInputFieldState: PassphraseInputFieldState,
    isNextButtonEnabled: Boolean,
    hideSupportText: Boolean,
    remainingConfirmationAttempts: Int,
    onUpdatePassphrase: (String) -> Unit,
    onCheckPassphrase: () -> Unit,
) {
    Spacer(modifier = Modifier.height(Sizes.s04))
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        PassphraseInputComponent(
            modifier = Modifier.weight(1f),
            colors = textFieldColors(),
            passphraseInputFieldState = passphraseInputFieldState,
            passphrase = passphrase,
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
            onPassphraseChange = onUpdatePassphrase,
            onAnimationFinished = {},
        )
        Spacer(modifier = Modifier.width(Sizes.s08))
        BottomButton(
            enabled = isNextButtonEnabled,
            onCheckPassphrase = onCheckPassphrase
        )
    }
}

@Composable
private fun textFieldColors() = TextFieldDefaults.colors().copy(
    focusedContainerColor = WalletTheme.colorScheme.background,
    unfocusedContainerColor = WalletTheme.colorScheme.background,
    errorContainerColor = WalletTheme.colorScheme.background,
    errorTextColor = WalletTheme.colorScheme.onSurfaceVariant,
    errorCursorColor = WalletTheme.colorScheme.onSurfaceVariant,
    errorTrailingIconColor = WalletTheme.colorScheme.onSurfaceVariant,
)

@Composable
private fun Label(
    passphraseInputFieldState: PassphraseInputFieldState,
) = WalletTexts.BodyLarge(
    text = stringResource(R.string.tk_changepassword_step1_note1),
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
) = Buttons.FilledPrimaryFixed(
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
            passphrase = "abc123",
            passphraseInputFieldState = PassphraseInputFieldState.Error,
            isNextButtonEnabled = true,
            hideSupportText = false,
            remainingConfirmationAttempts = 4,
            isLoading = false,
            onUpdatePassphrase = {},
            onCheckPassphrase = {},
        )
    }
}
