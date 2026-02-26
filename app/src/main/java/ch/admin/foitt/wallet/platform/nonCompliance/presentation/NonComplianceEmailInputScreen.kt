package ch.admin.foitt.wallet.platform.nonCompliance.presentation

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceEmailValidationState
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceInputFieldState
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceValidationState
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun NonComplianceEmailInputScreen(viewModel: NonComplianceEmailInputViewModel) {
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(keyboardController) {
        keyboardController?.show()
    }

    NonComplianceEmailInputScreenContent(
        textFieldValue = viewModel.textFieldValue.collectAsStateWithLifecycle().value,
        textFieldState = viewModel.textFieldState.collectAsStateWithLifecycle().value,
        validationState = viewModel.validationState.collectAsStateWithLifecycle().value,
        placeholder = R.string.tk_nonCompliance_report_form_contact_placeholder,
        onTextChange = viewModel::onTextFieldValueChange,
        onClearInput = viewModel::onClearInput,
        onContinue = viewModel::onBack,
    )
}

@Composable
private fun NonComplianceEmailInputScreenContent(
    textFieldState: NonComplianceInputFieldState,
    textFieldValue: TextFieldValue,
    validationState: NonComplianceEmailValidationState,
    @StringRes placeholder: Int,
    onTextChange: (TextFieldValue) -> Unit,
    onClearInput: () -> Unit,
    onContinue: () -> Unit,
) = NonComplianceFormInputScreenContent(
    textFieldState = textFieldState,
    textFieldValue = textFieldValue,
    isInputValid = validationState is NonComplianceValidationState.Valid,
    placeholder = placeholder,
    supportingText = {
        val supportingTextId = if (validationState is NonComplianceValidationState.Valid) {
            R.string.tk_nonCompliance_report_form_contact_footer
        } else {
            R.string.tk_nonCompliance_report_form_email_error
        }
        WalletTexts.BodySmall(
            text = stringResource(supportingTextId),
            color = Color.Unspecified
        )
    },
    onTextChange = onTextChange,
    onClearInput = onClearInput,
    onContinue = onContinue,
)

@WalletAllScreenPreview
@Composable
private fun NonComplianceEmailInputScreenPreview() {
    WalletTheme {
        NonComplianceEmailInputScreenContent(
            textFieldValue = TextFieldValue("abc123"),
            validationState = NonComplianceValidationState.Valid,
            placeholder = R.string.tk_nonCompliance_report_form_contact_placeholder,
            textFieldState = NonComplianceInputFieldState.Edited,
            onTextChange = {},
            onClearInput = {},
            onContinue = {},
        )
    }
}
