package ch.admin.foitt.wallet.platform.nonCompliance.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.WalletTextField
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceInputFieldState
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTextFieldColors
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NonComplianceTextInputComponent(
    @StringRes placeholder: Int,
    supportingText: @Composable RowScope.() -> Unit,
    textFieldValue: TextFieldValue,
    isInputValid: Boolean,
    nonComplianceInputFieldState: NonComplianceInputFieldState,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onTrailingIcon: () -> Unit,
    onKeyboardAction: () -> Unit,
    keyboardType: KeyboardType = KeyboardType.Unspecified,
    modifier: Modifier = Modifier,
) {
    WalletTextField.TextInputField(
        modifier = modifier,
        textFieldValue = textFieldValue,
        onTextFieldValueChange = onTextFieldValueChange,
        isError = !isInputValid && nonComplianceInputFieldState !is NonComplianceInputFieldState.Initial,
        placeholder = {
            WalletTexts.BodyLarge(stringResource(placeholder))
        },
        trailingIcon = {
            Icon(
                modifier = Modifier.clickable {
                    onTrailingIcon()
                },
                painter = painterResource(R.drawable.wallet_ic_circled_cross),
                contentDescription = stringResource(R.string.tk_nonCompliance_report_form_deleteText_button_alt),
            )
        },
        supportingText = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                supportingText()
            }
        },
        keyboardOptions = KeyboardOptions(
            autoCorrectEnabled = true,
            imeAction = ImeAction.Go,
            showKeyboardOnFocus = true,
            keyboardType = keyboardType,
        ),
        keyboardActions = KeyboardActions {
            onKeyboardAction()
        },
        colors = WalletTextFieldColors.textFieldColorsInCluster(),
    )
}

@WalletComponentPreview
@Composable
private fun NonComplianceTextInputComponentPreview() {
    WalletTheme {
        NonComplianceTextInputComponent(
            textFieldValue = TextFieldValue("abc123"),
            nonComplianceInputFieldState = NonComplianceInputFieldState.Initial,
            isInputValid = true,
            placeholder = R.string.tk_nonCompliance_report_form_description_placeholder,
            supportingText = {
                WalletTexts.BodySmall(
                    text = stringResource(R.string.tk_nonCompliance_report_form_description_footer),
                    color = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(Sizes.s02))
                WalletTexts.BodySmall(
                    text = "6/500",
                    color = Color.Unspecified,
                )
            },
            onKeyboardAction = {},
            onTextFieldValueChange = {},
            onTrailingIcon = {},
        )
    }
}
