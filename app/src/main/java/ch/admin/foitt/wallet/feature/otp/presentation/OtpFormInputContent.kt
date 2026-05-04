package ch.admin.foitt.wallet.feature.otp.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.otp.domain.model.OtpInputFieldState
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.ToastAnimated
import ch.admin.foitt.wallet.platform.composables.presentation.HeightReportingLayout
import ch.admin.foitt.wallet.platform.composables.presentation.WindowWidthClass
import ch.admin.foitt.wallet.platform.composables.presentation.horizontalSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.layout.LazyColumn
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.composables.presentation.verticalSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.windowWidthClass
import ch.admin.foitt.wallet.platform.scaffold.presentation.LocalScaffoldPaddings
import ch.admin.foitt.wallet.theme.Sizes

@Composable
fun OtpFormInputContent(
    textFieldState: OtpInputFieldState? = null,
    textFieldValue: TextFieldValue,
    inputMaxLength: Int = 100,
    isInputValid: Boolean,
    @StringRes placeholder: Int,
    @StringRes label: Int,
    isToastVisible: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Email,
    supportingText: @Composable ColumnScope.() -> Unit,
    onTextChange: (TextFieldValue) -> Unit,
    onContinue: (() -> Unit)? = null,
    onBypassOtp: (() -> Unit)? = null
) = when (currentWindowAdaptiveInfo().windowWidthClass()) {
    WindowWidthClass.COMPACT -> FormInputCompactContent(
        textFieldState = textFieldState,
        textFieldValue = textFieldValue,
        inputMaxLength = inputMaxLength,
        isInputValid = isInputValid,
        placeholder = placeholder,
        label = label,
        isToastVisible = isToastVisible,
        keyboardType = keyboardType,
        supportingText = supportingText,
        onTextChange = onTextChange,
        onContinue = onContinue,
        onBypassOtp = onBypassOtp
    )

    else -> FormInputLargeContent(
        textFieldState = textFieldState,
        textFieldValue = textFieldValue,
        inputMaxLength = inputMaxLength,
        isInputValid = isInputValid,
        placeholder = placeholder,
        label = label,
        isToastVisible = isToastVisible,
        keyboardType = keyboardType,
        supportingText = supportingText,
        onTextChange = onTextChange,
        onContinue = onContinue,
        onBypassOtp = onBypassOtp
    )
}

@Composable
private fun FormInputCompactContent(
    textFieldState: OtpInputFieldState?,
    textFieldValue: TextFieldValue,
    inputMaxLength: Int,
    isInputValid: Boolean,
    @StringRes placeholder: Int,
    @StringRes label: Int,
    isToastVisible: Boolean,
    keyboardType: KeyboardType = KeyboardType.Email,
    supportingText: @Composable ColumnScope.() -> Unit,
    onTextChange: (TextFieldValue) -> Unit,
    onContinue: (() -> Unit)?,
    onBypassOtp: (() -> Unit)?,
) = Box(
    modifier = Modifier
        .fillMaxSize()
) {
    val buttonHeight = remember { mutableStateOf(0.dp) }

    WalletLayouts.LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalSafeDrawing(),
        state = rememberLazyListState(),
        contentPadding = PaddingValues(horizontal = Sizes.s04),
        useTopInsets = false,
    ) {
        item {
            WalletLayouts.TopInsetSpacer(
                shouldScrollUnderTopBar = true,
                scaffoldPaddings = LocalScaffoldPaddings.current,
            )
        }

        item { Spacer(modifier = Modifier.height(Sizes.s02)) }

        item {
            OtpTextInputComponent(
                modifier = Modifier.fillMaxWidth(),
                textFieldValue = textFieldValue,
                inputMaxLength = inputMaxLength,
                otpInputFieldState = textFieldState,
                isInputValid = isInputValid,
                placeholder = placeholder,
                supportingText = supportingText,
                onTextFieldValueChange = onTextChange,
                onKeyboardAction = {
                    if (isInputValid && onContinue != null) {
                        onContinue()
                    }
                },
                keyboardType = keyboardType,
                label = label,
            )
        }

        item { Spacer(modifier = Modifier.height(Sizes.s02 + buttonHeight.value)) }
    }

    HeightReportingLayout(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .horizontalSafeDrawing()
            .verticalSafeDrawing(),
        onContentHeightMeasured = { measuredHeight ->
            buttonHeight.value = measuredHeight
        }
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Sizes.s04)
                .padding(bottom = Sizes.s04)
        ) {
            ToastAnimated(
                modifier = Modifier,
                isVisible = isToastVisible,
                isSnackBarDesign = false,
                messageToast = R.string.tk_eidRequest_otp_code_toast_expired,
                contentBottomPadding = Sizes.s02
            )

            if (onBypassOtp != null) {
                Buttons.TonalSecondary(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.tk_eidRequest_otp_email_skip_button),
                    onClick = onBypassOtp,
                )
                Spacer(modifier = Modifier.height(Sizes.s04))
            }
            if (onContinue != null) {
                Buttons.FilledPrimary(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.tk_eidRequest_otp_email_primaryButton),
                    enabled = isInputValid,
                    onClick = onContinue,
                )
            }
        }
    }
}

@Composable
private fun FormInputLargeContent(
    textFieldState: OtpInputFieldState?,
    textFieldValue: TextFieldValue,
    inputMaxLength: Int,
    isInputValid: Boolean,
    @StringRes placeholder: Int,
    @StringRes label: Int,
    isToastVisible: Boolean,
    keyboardType: KeyboardType = KeyboardType.Email,
    supportingText: @Composable ColumnScope.() -> Unit,
    onTextChange: (TextFieldValue) -> Unit,
    onContinue: (() -> Unit)?,
    onBypassOtp: (() -> Unit)?
) = Box(
    modifier = Modifier
        .fillMaxSize()
) {
    WalletLayouts.LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalSafeDrawing(),
        state = rememberLazyListState(),
        contentPadding = PaddingValues(horizontal = Sizes.s04),
        useTopInsets = false,
    ) {
        item {
            WalletLayouts.TopInsetSpacer(
                shouldScrollUnderTopBar = true,
                scaffoldPaddings = LocalScaffoldPaddings.current,
            )
        }

        item { Spacer(modifier = Modifier.height(Sizes.s02)) }

        item {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                OtpTextInputComponent(
                    modifier = Modifier.weight(1f),
                    textFieldValue = textFieldValue,
                    inputMaxLength = inputMaxLength,
                    otpInputFieldState = textFieldState,
                    isInputValid = isInputValid,
                    placeholder = placeholder,
                    supportingText = supportingText,
                    onTextFieldValueChange = onTextChange,
                    onKeyboardAction = {
                        if (isInputValid && onContinue != null) {
                            onContinue()
                        }
                    },
                    keyboardType = keyboardType,
                    label = label
                )

                if (onContinue != null) {
                    Spacer(modifier = Modifier.width(Sizes.s04))
                    Buttons.FilledPrimary(
                        text = stringResource(R.string.tk_eidRequest_otp_email_primaryButton),
                        enabled = isInputValid,
                        onClick = onContinue,
                    )
                }
            }
        }

        item {
            ToastAnimated(
                modifier = Modifier,
                isVisible = isToastVisible,
                isSnackBarDesign = false,
                messageToast = R.string.tk_eidRequest_otp_code_toast_expired,
                contentBottomPadding = Sizes.s02
            )
        }

        item { Spacer(modifier = Modifier.height(Sizes.s02)) }

        item {
            if (onBypassOtp != null) {
                Buttons.TonalSecondary(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.tk_eidRequest_otp_email_skip_button),
                    onClick = onBypassOtp,
                )
            }
        }
    }
}
