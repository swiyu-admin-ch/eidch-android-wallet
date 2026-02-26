package ch.admin.foitt.wallet.platform.nonCompliance.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.WalletTextField
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceInputFieldState
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceInputFieldType
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTextFieldColors
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NonComplianceTextInputComponentReadOnly(
    @StringRes placeholder: Int,
    textFieldValue: TextFieldValue,
    nonComplianceInputFieldState: NonComplianceInputFieldState,
    nonComplianceInputFieldType: NonComplianceInputFieldType,
    maxInputLength: Int,
    isError: Boolean,
    onClick: (NonComplianceInputFieldType) -> Unit,
    onTrailingIcon: () -> Unit,
    modifier: Modifier = Modifier,
    @StringRes supportingText: Int? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }

    // intercept click events on the text input field
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                onClick(nonComplianceInputFieldType)
            }
        }
    }

    WalletTextField.TextInputField(
        textFieldValue = textFieldValue,
        onTextFieldValueChange = {},
        modifier = modifier
            .clickable( // intercept click events when talkback is enabled
                interactionSource = interactionSource,
                indication = null,
                onClick = { onClick(nonComplianceInputFieldType) }
            ),
        readOnly = true,
        isError = isError && nonComplianceInputFieldState !is NonComplianceInputFieldState.Initial,
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
            supportingText?.let {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    WalletTexts.BodySmall(
                        modifier = Modifier.weight(1f),
                        text = stringResource(it),
                        color = Color.Unspecified
                    )
                    if (nonComplianceInputFieldType == NonComplianceInputFieldType.DESCRIPTION) {
                        Spacer(modifier = Modifier.width(Sizes.s02))
                        WalletTexts.BodySmall(
                            text = "${textFieldValue.text.length}/$maxInputLength",
                            color = Color.Unspecified,
                        )
                    }
                }
            }
        },
        interactionSource = interactionSource,
        colors = WalletTextFieldColors.textFieldColorsInCluster(),
    )
}

@WalletComponentPreview
@Composable
private fun NonComplianceTextInputComponentPreview() {
    WalletTheme {
        NonComplianceTextInputComponentReadOnly(
            textFieldValue = TextFieldValue("abc123"),
            nonComplianceInputFieldState = NonComplianceInputFieldState.Initial,
            nonComplianceInputFieldType = NonComplianceInputFieldType.DESCRIPTION,
            maxInputLength = 500,
            isError = true,
            placeholder = R.string.tk_nonCompliance_report_form_description_placeholder,
            supportingText = R.string.tk_nonCompliance_report_form_description_footer,
            onTrailingIcon = {},
            onClick = {},
        )
    }
}
