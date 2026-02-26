package ch.admin.foitt.wallet.feature.login.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.PassphraseValidationErrorToastFixed
import ch.admin.foitt.wallet.platform.composables.presentation.WindowWidthClass
import ch.admin.foitt.wallet.platform.composables.presentation.centerHorizontallyOnFullscreen
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.composables.presentation.windowWidthClass
import ch.admin.foitt.wallet.platform.login.domain.Constants.MAX_LOGIN_ATTEMPTS
import ch.admin.foitt.wallet.platform.passphraseInput.domain.model.PassphraseInputFieldState
import ch.admin.foitt.wallet.platform.passphraseInput.presentation.PassphraseInputComponent
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.scaffold.presentation.FullscreenGradient
import ch.admin.foitt.wallet.platform.utils.LocalActivity
import ch.admin.foitt.wallet.platform.utils.OnResumeEventHandler
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTextFieldColors
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun PassphraseLoginScreen(
    viewModel: PassphraseLoginViewModel,
) {
    OnResumeEventHandler {
        viewModel.canUseBiometrics()
    }

    val activity = LocalActivity.current
    BackHandler {
        viewModel.navigateBack(activity)
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val isLoading = viewModel.isLoading.collectAsStateWithLifecycle().value

    LaunchedEffect(isLoading) {
        when (isLoading) {
            true -> keyboardController?.hide()
            false -> keyboardController?.show()
        }
    }

    PassphraseLoginScreenContent(
        textFieldValue = viewModel.textFieldValue.collectAsStateWithLifecycle().value,
        passphraseInputFieldState = viewModel.passphraseInputFieldState.collectAsStateWithLifecycle().value,
        loginAttemptsLeft = viewModel.loginAttemptsLeft.collectAsStateWithLifecycle().value,
        showPassphraseErrorToast = viewModel.showPassphraseErrorToast.collectAsStateWithLifecycle().value,
        showBiometricsLoginButton = viewModel.showBiometricLoginButton.collectAsStateWithLifecycle().value,
        isLoading = isLoading,
        onTextFieldValueChange = viewModel::onTextFieldValueChange,
        onLoginWithPassphrase = viewModel::onLoginWithPassphrase,
        onLoginWithBiometrics = viewModel::onLoginWithBiometrics,
        onClosePassphraseError = viewModel::onClosePassphraseError,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PassphraseLoginScreenContent(
    textFieldValue: TextFieldValue,
    passphraseInputFieldState: PassphraseInputFieldState,
    loginAttemptsLeft: Int,
    showPassphraseErrorToast: Boolean,
    showBiometricsLoginButton: Boolean,
    isLoading: Boolean,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onLoginWithPassphrase: () -> Unit,
    onLoginWithBiometrics: () -> Unit,
    onClosePassphraseError: () -> Unit,
) {
    FullscreenGradient()

    when (currentWindowAdaptiveInfo().windowWidthClass()) {
        WindowWidthClass.COMPACT -> WalletLayouts.CompactContainerFloatingBottom(
            verticalArrangement = Arrangement.Top,
            content = {
                CompactContent(
                    textFieldValue = textFieldValue,
                    passphraseInputFieldState = passphraseInputFieldState,
                    loginAttemptsLeft = loginAttemptsLeft,
                    isLoading = isLoading,
                    onTextFieldValueChange = onTextFieldValueChange,
                    onLoginWithPassphrase = onLoginWithPassphrase,
                )
            },
            auxiliaryContent = {
                AuxiliaryContent(
                    passphraseInputFieldState = passphraseInputFieldState,
                    showPassphraseErrorToast = showPassphraseErrorToast,
                    isLoading = isLoading,
                    onClosePassphraseError = onClosePassphraseError,
                )
            },
            stickyBottomContent = {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Sizes.s04, Alignment.End),
                    verticalArrangement = Arrangement.spacedBy(Sizes.s02)
                ) {
                    if (showBiometricsLoginButton) {
                        Buttons.FilledSecondaryFixed(
                            text = stringResource(R.string.tk_global_loginbiometric_primarybutton),
                            startIcon = painterResource(R.drawable.ic_fingerprint),
                            onClick = onLoginWithBiometrics
                        )
                    }
                    Buttons.FilledPrimaryFixed(
                        text = stringResource(R.string.tk_global_login_primarybutton),
                        onClick = onLoginWithPassphrase
                    )
                }
            },
        )

        else -> WalletLayouts.LargeContainerFloatingBottom(
            content = {
                LargeContent(
                    textFieldValue = textFieldValue,
                    passphraseInputFieldState = passphraseInputFieldState,
                    loginAttemptsLeft = loginAttemptsLeft,
                    showBiometricsLoginButton = showBiometricsLoginButton,
                    isLoading = isLoading,
                    onTextFieldValueChange = onTextFieldValueChange,
                    onLoginWithPassphrase = onLoginWithPassphrase,
                    onLoginWithBiometrics = onLoginWithBiometrics,
                )
            },
            auxiliaryContent = {
                AuxiliaryContent(
                    passphraseInputFieldState = passphraseInputFieldState,
                    showPassphraseErrorToast = showPassphraseErrorToast,
                    isLoading = isLoading,
                    onClosePassphraseError = onClosePassphraseError,
                )
            }
        )
    }
    LoadingOverlay(
        showOverlay = isLoading,
        color = WalletTheme.colorScheme.primaryFixed,
    )
}

@Composable
private fun CompactContent(
    textFieldValue: TextFieldValue,
    passphraseInputFieldState: PassphraseInputFieldState,
    loginAttemptsLeft: Int,
    isLoading: Boolean,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onLoginWithPassphrase: () -> Unit,
) {
    Spacer(modifier = Modifier.height(Sizes.s12))
    Content()
    Spacer(modifier = Modifier.height(Sizes.s06))
    PassphraseInputComponent(
        modifier = Modifier.fillMaxWidth(),
        passphraseInputFieldState = passphraseInputFieldState,
        textFieldValue = textFieldValue,
        colors = WalletTextFieldColors.textFieldColorsFixed(),
        keyboardImeAction = ImeAction.Go,
        onKeyboardAction = onLoginWithPassphrase,
        onTextFieldValueChange = onTextFieldValueChange,
        placeholder = {
            Placeholder()
        },
        supportingText = {
            SupportingText(
                isLoading = isLoading,
                loginAttemptsLeft = loginAttemptsLeft,
            )
        },
        onAnimationFinished = {},
    )
}

@Composable
private fun LargeContent(
    textFieldValue: TextFieldValue,
    passphraseInputFieldState: PassphraseInputFieldState,
    loginAttemptsLeft: Int,
    showBiometricsLoginButton: Boolean,
    isLoading: Boolean,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onLoginWithPassphrase: () -> Unit,
    onLoginWithBiometrics: () -> Unit,
) {
    Content()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Sizes.s04),
    ) {
        PassphraseInputComponent(
            modifier = Modifier.weight(1f),
            passphraseInputFieldState = passphraseInputFieldState,
            textFieldValue = textFieldValue,
            colors = WalletTextFieldColors.textFieldColorsFixed(),
            keyboardImeAction = ImeAction.Go,
            onKeyboardAction = onLoginWithPassphrase,
            onTextFieldValueChange = onTextFieldValueChange,
            placeholder = {
                Placeholder()
            },
            supportingText = {
                SupportingText(
                    isLoading = isLoading,
                    loginAttemptsLeft = loginAttemptsLeft,
                )
            },
            onAnimationFinished = {},
        )
        Spacer(modifier = Modifier.width(Sizes.s08))
        if (showBiometricsLoginButton) {
            Buttons.Icon(
                icon = R.drawable.ic_fingerprint,
                contentDescription = stringResource(R.string.tk_global_loginbiometric_primarybutton),
                onClick = onLoginWithBiometrics
            )
            Spacer(modifier = Modifier.width(Sizes.s02))
        }
        Buttons.FilledPrimaryFixed(
            text = stringResource(R.string.tk_global_login_primarybutton),
            onClick = onLoginWithPassphrase
        )
    }
}

@Composable
private fun Content() = Column(
    modifier = Modifier.centerHorizontallyOnFullscreen(),
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    WalletTexts.TitleLarge(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Sizes.s04),
        text = stringResource(R.string.tk_global_welcomeback),
        textAlign = TextAlign.Center,
        color = WalletTheme.colorScheme.onGradientFixed
    )
    Spacer(modifier = Modifier.height(Sizes.s02))
    WalletTexts.TitleSmall(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(R.string.tk_login_password_body),
        textAlign = TextAlign.Center,
        color = WalletTheme.colorScheme.onGradientFixed
    )
}

@Composable
private fun AuxiliaryContent(
    passphraseInputFieldState: PassphraseInputFieldState,
    showPassphraseErrorToast: Boolean,
    isLoading: Boolean,
    onClosePassphraseError: () -> Unit,
) {
    if (passphraseInputFieldState == PassphraseInputFieldState.Error && showPassphraseErrorToast && !isLoading) {
        PassphraseValidationErrorToastFixed(
            modifier = Modifier
                .padding(start = Sizes.s08, end = Sizes.s08, bottom = Sizes.s06),
            text = R.string.tk_login_passwordfailed_notification,
            onIconEnd = onClosePassphraseError,
        )
    }
}

@Composable
private fun Placeholder() = WalletTexts.BodyLarge(
    text = stringResource(R.string.tk_login_password_note),
    color = WalletTheme.colorScheme.onSurfaceVariantFixed
)

@Composable
private fun SupportingText(
    isLoading: Boolean,
    loginAttemptsLeft: Int,
) {
    val hideSupportText = isLoading || loginAttemptsLeft >= MAX_LOGIN_ATTEMPTS
    WalletTexts.BodySmall(
        modifier = Modifier
            .alpha(if (hideSupportText) 0f else 1f)
            .focusable(!hideSupportText),
        text = stringResource(R.string.tk_login_passwordfailed_android_subtitle, loginAttemptsLeft),
        color = WalletTheme.colorScheme.onGradientFixed
    )
}

@WalletAllScreenPreview
@Composable
private fun PassphraseLoginScreenPreview() {
    WalletTheme {
        PassphraseLoginScreenContent(
            textFieldValue = TextFieldValue("abc123"),
            passphraseInputFieldState = PassphraseInputFieldState.Typing,
            loginAttemptsLeft = 5,
            showPassphraseErrorToast = true,
            showBiometricsLoginButton = true,
            isLoading = false,
            onTextFieldValueChange = {},
            onLoginWithPassphrase = {},
            onLoginWithBiometrics = {},
            onClosePassphraseError = {},
        )
    }
}
