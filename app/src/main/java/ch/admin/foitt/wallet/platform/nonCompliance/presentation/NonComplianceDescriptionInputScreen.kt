package ch.admin.foitt.wallet.platform.nonCompliance.presentation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceInputFieldState
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceTextLengthValidationState
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceValidationState
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun NonComplianceDescriptionInputScreen(viewModel: NonComplianceDescriptionInputViewModel) {
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(keyboardController) {
        keyboardController?.show()
    }
    NonComplianceDescriptionInputScreenContent(
        textFieldState = viewModel.textFieldState.collectAsStateWithLifecycle().value,
        textFieldValue = viewModel.textFieldValue.collectAsStateWithLifecycle().value,
        validationState = viewModel.validationState.collectAsStateWithLifecycle().value,
        maxInputLength = viewModel.maxInputLength,
        onTextChange = viewModel::onTextFieldValueChange,
        onClearInput = viewModel::onClearInput,
        onContinue = viewModel::onBack,
    )
}

@Composable
private fun NonComplianceDescriptionInputScreenContent(
    textFieldState: NonComplianceInputFieldState,
    textFieldValue: TextFieldValue,
    validationState: NonComplianceTextLengthValidationState,
    maxInputLength: Int,
    onTextChange: (TextFieldValue) -> Unit,
    onClearInput: () -> Unit,
    onContinue: () -> Unit,
) = NonComplianceFormInputScreenContent(
    textFieldState = textFieldState,
    textFieldValue = textFieldValue,
    isInputValid = validationState is NonComplianceValidationState.Valid,
    placeholder = R.string.tk_nonCompliance_report_form_description_placeholder,
    supportingText = {
        val textId = when (validationState) {
            NonComplianceValidationState.Valid -> R.string.tk_nonCompliance_report_form_description_footer
            NonComplianceValidationState.TooShort -> R.string.tk_nonCompliance_report_form_description_footer
            NonComplianceValidationState.TooLong -> R.string.tk_nonCompliance_report_form_description_maxCharacter_footer
        }
        WalletTexts.BodySmall(
            text = stringResource(textId),
            color = Color.Unspecified
        )
        Spacer(modifier = Modifier.width(Sizes.s02))
        WalletTexts.BodySmall(
            text = "${textFieldValue.text.length}/$maxInputLength",
            color = Color.Unspecified,
        )
    },
    onTextChange = onTextChange,
    onClearInput = onClearInput,
    onContinue = onContinue,
)

@WalletAllScreenPreview
@Composable
private fun NonComplianceFormInputScreenPreview() {
    WalletTheme {
        NonComplianceDescriptionInputScreenContent(
            textFieldValue = TextFieldValue("abc123"),
            validationState = NonComplianceValidationState.Valid,
            textFieldState = NonComplianceInputFieldState.Edited,
            maxInputLength = 500,
            onTextChange = {},
            onClearInput = {},
            onContinue = {},
        )
    }
}
