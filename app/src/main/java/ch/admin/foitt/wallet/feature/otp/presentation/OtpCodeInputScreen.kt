package ch.admin.foitt.wallet.feature.otp.presentation

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.InvalidClientContent
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.NetworkErrorContent
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.UnexpectedErrorContent
import ch.admin.foitt.wallet.feature.otp.domain.model.OtpCodeLengthValidationState
import ch.admin.foitt.wallet.feature.otp.domain.model.OtpValidationState
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.presentation.ScreenMainImage
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun OtpCodeInputScreen(viewModel: OtpCodeInputViewModel) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(uiState) {
        when (uiState) {
            OtpCodeInputUiState.Loading -> keyboardController?.hide()
            OtpCodeInputUiState.Initial,
            OtpCodeInputUiState.WrongCode -> keyboardController?.show()
            else -> {}
        }
    }

    BackHandler {
        viewModel.onBack()
    }

    OtpCodeInputScreenContent(
        uiState = uiState,
        textFieldValue = viewModel.textFieldValue.collectAsStateWithLifecycle().value,
        validationState = viewModel.validationState.collectAsStateWithLifecycle().value,
        placeholder = R.string.tk_eidRequest_otp_code_field_placeholder,
        label = R.string.tk_eidRequest_otp_code_field_title,
        email = viewModel.email,
        onTextChange = viewModel::onTextFieldValueChange,
        onContinue = viewModel::onContinue,
        onBackHome = viewModel::onBackHome,
        onStatusPage = viewModel::onStatusPage,
        onHelp = viewModel::onHelp,
        onPlaystore = viewModel::onPlaystore,
    )
}

@Composable
private fun OtpCodeInputScreenContent(
    uiState: OtpCodeInputUiState,
    textFieldValue: TextFieldValue,
    validationState: OtpCodeLengthValidationState,
    @StringRes placeholder: Int,
    @StringRes label: Int,
    email: String,
    onTextChange: (TextFieldValue) -> Unit,
    onContinue: () -> Unit,
    onBackHome: () -> Unit,
    onStatusPage: () -> Unit,
    onHelp: () -> Unit,
    onPlaystore: () -> Unit,
) {
    when (uiState) {
        OtpCodeInputUiState.Loading,
        OtpCodeInputUiState.Initial,
        OtpCodeInputUiState.WrongCode -> InitialContent(
            uiState = uiState,
            textFieldValue = textFieldValue,
            validationState = validationState,
            placeholder = placeholder,
            label = label,
            email = email,
            onTextChange = onTextChange,
        )

        OtpCodeInputUiState.Unavailable -> UnavailableContent(
            onButtonClick = onBackHome,
            onStatusPage = onStatusPage
        )

        OtpCodeInputUiState.Unexpected -> UnexpectedErrorContent(
            onClose = onBackHome,
            onRetry = onContinue
        )

        OtpCodeInputUiState.NetworkError -> NetworkErrorContent(
            titleText = R.string.tk_error_generic_primary,
            bodyText = R.string.tk_error_generic_secondary,
            onClose = onBackHome,
            onRetry = onContinue,
        )

        OtpCodeInputUiState.NotSupported -> InvalidClientContent(
            onClose = onBackHome,
            onHelp = onHelp,
            onPlaystore = onPlaystore
        )

        OtpCodeInputUiState.TooManyAttempts -> TooManyContent(
            onButtonClick = onBackHome
        )
    }
}

@Composable
private fun InitialContent(
    uiState: OtpCodeInputUiState,
    textFieldValue: TextFieldValue,
    validationState: OtpCodeLengthValidationState,
    @StringRes placeholder: Int,
    @StringRes label: Int,
    email: String,
    onTextChange: (TextFieldValue) -> Unit,
) {
    OtpFormInputContent(
        textFieldValue = textFieldValue,
        inputMaxLength = 6,
        isInputValid = validationState is OtpValidationState.Valid && !textFieldValue.text.isEmpty(),
        placeholder = placeholder,
        label = label,
        supportingText = {
            when (uiState) {
                OtpCodeInputUiState.WrongCode -> {
                    WalletTexts.BodySmall(
                        text = stringResource(R.string.tk_eidRequest_otp_code_error_invalid),
                        color = WalletTheme.colorScheme.error
                    )
                }

                else -> {
                    WalletTexts.BodySmall(
                        text = stringResource(R.string.tk_eidRequest_otp_code_body_sent, email),
                        color = WalletTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(Sizes.s02))
                    WalletTexts.BodySmall(
                        text = stringResource(R.string.tk_eidRequest_otp_code_body_validity),
                        color = WalletTheme.colorScheme.secondary,
                    )
                    Spacer(modifier = Modifier.height(Sizes.s02))
                    WalletTexts.BodySmall(
                        text = stringResource(R.string.tk_eidRequest_otp_code_body_help),
                        color = WalletTheme.colorScheme.secondary,
                    )
                }
            }
        },
        onTextChange = onTextChange,
        keyboardType = KeyboardType.Number
    )
    LoadingOverlay(uiState == OtpCodeInputUiState.Loading)
}

@Composable
private fun UnavailableContent(
    onButtonClick: () -> Unit,
    onStatusPage: () -> Unit,
) = ErrorContent(
    onButtonClick = onButtonClick,
    onStatusPage = onStatusPage,
    title = R.string.tk_eidRequest_otp_unavailable_title,
    bodyPrimary = R.string.tk_eidRequest_otp_unavailable_body,
    buttonText = R.string.tk_eidRequest_otp_unavailable_primaryButton,
    mainImage = R.drawable.wallet_ic_hourglass_colored
)

@Composable
private fun TooManyContent(
    onButtonClick: () -> Unit,
) = ErrorContent(
    onButtonClick = onButtonClick,
    title = R.string.tk_eidRequest_otp_tooManyAttempts_title,
    bodyPrimary = R.string.tk_eidRequest_otp_tooManyAttempts_body_primary,
    bodySecondary = R.string.tk_eidRequest_otp_tooManyAttempts_body_secondary,
    buttonText = R.string.tk_eidRequest_otp_tooManyAttempts_button,
    mainImage = R.drawable.wallet_ic_cross_circle_colored
)

@Composable
private fun ErrorContent(
    onButtonClick: () -> Unit,
    onStatusPage: (() -> Unit)? = null,
    @StringRes title: Int,
    @StringRes bodyPrimary: Int,
    @StringRes bodySecondary: Int? = null,
    @StringRes buttonText: Int,
    @DrawableRes mainImage: Int,
) = WalletLayouts.ScrollableColumnWithPicture(
    stickyStartContent = {
        ScreenMainImage(
            iconRes = mainImage,
            backgroundColor = WalletTheme.colorScheme.surfaceContainerLow
        )
    },
    stickyBottomBackgroundColor = Color.Transparent,
    stickyBottomContent = {
        Buttons.FilledPrimary(
            text = stringResource(buttonText),
            onClick = onButtonClick,
            modifier = Modifier.fillMaxWidth(),
        )
    },
) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleScreen(
        text = stringResource(title)
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.BodyLarge(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(bodyPrimary)
    )
    if (bodySecondary != null) {
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.BodyLarge(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(bodySecondary)
        )
    }
    if (onStatusPage != null) {
        Spacer(modifier = Modifier.height(Sizes.s06))
        Buttons.TextLink(
            text = stringResource(R.string.tk_eidRequest_otp_unavailable_link_text),
            onClick = onStatusPage,
            endIcon = painterResource(id = R.drawable.wallet_ic_external_link),
        )
    }
}

@WalletAllScreenPreview
@Composable
private fun OtpCodeInputScreenPreview() {
    WalletTheme {
        OtpCodeInputScreenContent(
            uiState = OtpCodeInputUiState.Initial,
            textFieldValue = TextFieldValue("123456"),
            validationState = OtpValidationState.Valid,
            placeholder = R.string.tk_eidRequest_otp_code_field_placeholder,
            label = R.string.tk_eidRequest_otp_code_field_title,
            email = "email@email.ch",
            onTextChange = {},
            onContinue = {},
            onBackHome = {},
            onStatusPage = {},
            onHelp = {},
            onPlaystore = {},
        )
    }
}
