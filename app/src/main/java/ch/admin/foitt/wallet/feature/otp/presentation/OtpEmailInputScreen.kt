package ch.admin.foitt.wallet.feature.otp.presentation

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.InvalidClientContent
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.NetworkErrorContent
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.UnexpectedErrorContent
import ch.admin.foitt.wallet.feature.otp.domain.model.OtpEmailValidationState
import ch.admin.foitt.wallet.feature.otp.domain.model.OtpInputFieldState
import ch.admin.foitt.wallet.feature.otp.domain.model.OtpValidationState
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.presentation.ScreenMainImage
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.utils.OnResumeEventHandler
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun OtpEmailInputScreen(viewModel: OtpEmailInputViewModel) {
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(keyboardController) {
        keyboardController?.show()
    }

    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(uiState) {
        when (uiState) {
            OtpEmailInputUiState.Loading -> keyboardController?.hide()
            OtpEmailInputUiState.Initial,
            OtpEmailInputUiState.ForbiddenEmail -> keyboardController?.show()
            else -> {}
        }
    }

    BackHandler {
        viewModel.onBack()
    }

    OnResumeEventHandler {
        viewModel.onResume()
    }

    OtpEmailInputScreenContent(
        uiState = uiState,
        textFieldValue = viewModel.textFieldValue.collectAsStateWithLifecycle().value,
        textFieldState = viewModel.emailInputFieldState.collectAsStateWithLifecycle().value,
        validationState = viewModel.validationState.collectAsStateWithLifecycle().value,
        isToastVisible = viewModel.isToastVisible.collectAsStateWithLifecycle().value,
        placeholder = R.string.tk_eidRequest_otp_email_field_placeholder,
        label = R.string.tk_eidRequest_otp_email_field_title,
        onTextChange = viewModel::onTextFieldValueChange,
        onContinue = viewModel::onContinue,
        onBackHome = viewModel::onBackHome,
        onStatusPage = viewModel::onStatusPage,
        onHelp = viewModel::onHelp,
        onPlaystore = viewModel::onPlaystore,
        onBypassOtp = if (viewModel.allowBypassOtp) viewModel::onBypassOtp else null,
    )
}

@Composable
private fun OtpEmailInputScreenContent(
    uiState: OtpEmailInputUiState,
    textFieldState: OtpInputFieldState,
    textFieldValue: TextFieldValue,
    validationState: OtpEmailValidationState,
    isToastVisible: Boolean,
    @StringRes placeholder: Int,
    @StringRes label: Int,
    onTextChange: (TextFieldValue) -> Unit,
    onContinue: () -> Unit,
    onBackHome: () -> Unit,
    onStatusPage: () -> Unit,
    onHelp: () -> Unit,
    onPlaystore: () -> Unit,
    onBypassOtp: (() -> Unit)?
) {
    when (uiState) {
        OtpEmailInputUiState.Loading,
        OtpEmailInputUiState.Initial,
        OtpEmailInputUiState.ForbiddenEmail -> InitialContent(
            uiState = uiState,
            textFieldState = textFieldState,
            textFieldValue = textFieldValue,
            validationState = validationState,
            isToastVisible = isToastVisible,
            placeholder = placeholder,
            label = label,
            onTextChange = onTextChange,
            onContinue = onContinue,
            onBypassOtp = onBypassOtp
        )

        OtpEmailInputUiState.Unavailable -> UnavailableContent(
            onButtonClick = onBackHome,
            onStatusPage = onStatusPage
        )

        OtpEmailInputUiState.Unexpected -> UnexpectedErrorContent(
            onClose = onBackHome,
            onRetry = onContinue
        )

        OtpEmailInputUiState.NetworkError -> NetworkErrorContent(
            titleText = R.string.tk_error_generic_primary,
            bodyText = R.string.tk_error_generic_secondary,
            onClose = onBackHome,
            onRetry = onContinue,
        )

        OtpEmailInputUiState.NotSupported -> InvalidClientContent(
            onClose = onBackHome,
            onHelp = onHelp,
            onPlaystore = onPlaystore
        )
    }
}

@Composable
private fun InitialContent(
    uiState: OtpEmailInputUiState,
    textFieldState: OtpInputFieldState,
    textFieldValue: TextFieldValue,
    validationState: OtpEmailValidationState,
    isToastVisible: Boolean,
    @StringRes placeholder: Int,
    @StringRes label: Int,
    onTextChange: (TextFieldValue) -> Unit,
    onContinue: () -> Unit,
    onBypassOtp: (() -> Unit)?
) {
    OtpFormInputContent(
        textFieldState = textFieldState,
        textFieldValue = textFieldValue,
        isInputValid = validationState is OtpValidationState.Valid && !textFieldValue.text.isEmpty(),
        placeholder = placeholder,
        label = label,
        isToastVisible = isToastVisible,
        supportingText = {
            when (uiState) {
                OtpEmailInputUiState.ForbiddenEmail -> {
                    WalletTexts.BodySmall(
                        text = stringResource(R.string.tk_eidRequest_otp_email_error_forbidden),
                        color = WalletTheme.colorScheme.error
                    )
                }

                else -> {
                    if (validationState is OtpValidationState.Valid) {
                        WalletTexts.BodySmall(
                            text = stringResource(R.string.tk_eidRequest_otp_email_body_primary),
                            color = WalletTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(Sizes.s02))
                        WalletTexts.BodySmall(
                            text = stringResource(R.string.tk_eidRequest_otp_email_body_secondary),
                            color = WalletTheme.colorScheme.secondary,
                        )
                    } else {
                        WalletTexts.BodySmall(
                            text = stringResource(R.string.tk_eidRequest_otp_email_error_invalid_format),
                            color = WalletTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        onTextChange = onTextChange,
        onContinue = onContinue,
        onBypassOtp = onBypassOtp
    )
    LoadingOverlay(uiState == OtpEmailInputUiState.Loading)
}

@Composable
private fun UnavailableContent(
    onButtonClick: () -> Unit,
    onStatusPage: () -> Unit,
) = WalletLayouts.ScrollableColumnWithPicture(
    stickyStartContent = {
        ScreenMainImage(
            iconRes = R.drawable.wallet_ic_hourglass_colored,
            backgroundColor = WalletTheme.colorScheme.surfaceContainerLow
        )
    },
    stickyBottomBackgroundColor = Color.Transparent,
    stickyBottomContent = {
        Buttons.FilledPrimary(
            text = stringResource(R.string.tk_eidRequest_otp_unavailable_primaryButton),
            onClick = onButtonClick,
            modifier = Modifier.fillMaxWidth(),
        )
    },
) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleScreen(
        text = stringResource(R.string.tk_eidRequest_otp_unavailable_title)
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.BodyLarge(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(R.string.tk_eidRequest_otp_unavailable_body)
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    Buttons.TextLink(
        text = stringResource(R.string.tk_eidRequest_otp_unavailable_link_text),
        onClick = onStatusPage,
        endIcon = painterResource(id = R.drawable.wallet_ic_external_link),
    )
}

@WalletAllScreenPreview
@Composable
private fun OtpEmailInputScreenPreview() {
    WalletTheme {
        OtpEmailInputScreenContent(
            uiState = OtpEmailInputUiState.Initial,
            textFieldValue = TextFieldValue("abc123"),
            validationState = OtpValidationState.Valid,
            placeholder = R.string.tk_eidRequest_otp_email_field_placeholder,
            isToastVisible = true,
            label = R.string.tk_eidRequest_otp_email_field_title,
            textFieldState = OtpInputFieldState.Initial,
            onTextChange = {},
            onContinue = {},
            onBackHome = {},
            onStatusPage = {},
            onHelp = {},
            onPlaystore = {},
            onBypassOtp = {}
        )
    }
}
