package ch.admin.foitt.wallet.feature.otp.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.otp.domain.model.OtpInputFieldState
import ch.admin.foitt.wallet.platform.composables.WalletTextField
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTextFieldColors
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun OtpTextInputComponent(
    modifier: Modifier = Modifier,
    @StringRes placeholder: Int,
    @StringRes label: Int,
    supportingText: @Composable ColumnScope.() -> Unit,
    textFieldValue: TextFieldValue,
    isInputValid: Boolean,
    inputMaxLength: Int,
    otpInputFieldState: OtpInputFieldState? = null,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onKeyboardAction: () -> Unit,
    keyboardType: KeyboardType = KeyboardType.Email,
) {
    WalletTextField.TextInputField(
        modifier = modifier,
        textFieldValue = textFieldValue,
        onTextFieldValueChange = {
            if (keyboardType != KeyboardType.Number || it.text.length <= inputMaxLength) {
                onTextFieldValueChange(it)
            }
        },
        isError = if (otpInputFieldState == null) {
            false
        } else {
            !isInputValid && otpInputFieldState !is OtpInputFieldState.Initial
        },
        placeholder = {
            WalletTexts.BodyLarge(stringResource(placeholder))
        },
        label = {
            WalletTexts.BodySmall(stringResource(label))
        },
        supportingText = {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                supportingText()
            }
        },
        keyboardOptions = KeyboardOptions(
            autoCorrectEnabled = true,
            imeAction = ImeAction.Send,
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
        OtpTextInputComponent(
            textFieldValue = TextFieldValue("abc123"),
            otpInputFieldState = OtpInputFieldState.Initial,
            inputMaxLength = 2000,
            isInputValid = true,
            placeholder = R.string.tk_eidRequest_otp_email_field_placeholder,
            label = R.string.tk_eidRequest_otp_email_field_title,
            supportingText = {
                WalletTexts.BodySmall(
                    text = stringResource(R.string.tk_eidRequest_otp_email_body_primary),
                    color = Color.Unspecified
                )
                Spacer(modifier = Modifier.height(Sizes.s02))
                WalletTexts.BodySmall(
                    text = stringResource(R.string.tk_eidRequest_otp_email_body_secondary),
                    color = Color.Unspecified,
                )
            },
            onKeyboardAction = {},
            onTextFieldValueChange = {}
        )
    }
}
