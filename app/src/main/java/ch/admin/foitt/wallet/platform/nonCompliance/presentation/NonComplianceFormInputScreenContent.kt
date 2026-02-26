package ch.admin.foitt.wallet.platform.nonCompliance.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.HeightReportingLayout
import ch.admin.foitt.wallet.platform.composables.presentation.WindowWidthClass
import ch.admin.foitt.wallet.platform.composables.presentation.horizontalSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.layout.LazyColumn
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.composables.presentation.verticalSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.windowWidthClass
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceInputFieldState
import ch.admin.foitt.wallet.platform.scaffold.presentation.LocalScaffoldPaddings
import ch.admin.foitt.wallet.theme.Sizes

@Composable
fun NonComplianceFormInputScreenContent(
    textFieldState: NonComplianceInputFieldState,
    textFieldValue: TextFieldValue,
    isInputValid: Boolean,
    @StringRes placeholder: Int,
    supportingText: @Composable RowScope.() -> Unit,
    onTextChange: (TextFieldValue) -> Unit,
    onClearInput: () -> Unit,
    onContinue: () -> Unit,
) = when (currentWindowAdaptiveInfo().windowWidthClass()) {
    WindowWidthClass.COMPACT -> FormInputCompactContent(
        textFieldState = textFieldState,
        textFieldValue = textFieldValue,
        isInputValid = isInputValid,
        placeholder = placeholder,
        supportingText = supportingText,
        onTextChange = onTextChange,
        onClearInput = onClearInput,
        onContinue = onContinue,
    )

    else -> FormInputLargeContent(
        textFieldState = textFieldState,
        textFieldValue = textFieldValue,
        isInputValid = isInputValid,
        placeholder = placeholder,
        supportingText = supportingText,
        onTextChange = onTextChange,
        onClearInput = onClearInput,
        onContinue = onContinue,
    )
}

@Composable
private fun FormInputCompactContent(
    textFieldState: NonComplianceInputFieldState,
    textFieldValue: TextFieldValue,
    isInputValid: Boolean,
    @StringRes placeholder: Int,
    supportingText: @Composable RowScope.() -> Unit,
    onTextChange: (TextFieldValue) -> Unit,
    onClearInput: () -> Unit,
    onContinue: () -> Unit,
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
            NonComplianceTextInputComponent(
                modifier = Modifier.fillMaxWidth(),
                textFieldValue = textFieldValue,
                nonComplianceInputFieldState = textFieldState,
                isInputValid = isInputValid,
                placeholder = placeholder,
                supportingText = supportingText,
                onTextFieldValueChange = onTextChange,
                onTrailingIcon = onClearInput,
                onKeyboardAction = onContinue,
                keyboardType = KeyboardType.Email,
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
        Buttons.FilledPrimary(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Sizes.s04)
                .padding(bottom = Sizes.s04),
            text = stringResource(R.string.tk_global_continue),
            enabled = isInputValid,
            onClick = onContinue,
        )
    }
}

@Composable
private fun FormInputLargeContent(
    textFieldState: NonComplianceInputFieldState,
    textFieldValue: TextFieldValue,
    isInputValid: Boolean,
    @StringRes placeholder: Int,
    supportingText: @Composable RowScope.() -> Unit,
    onTextChange: (TextFieldValue) -> Unit,
    onClearInput: () -> Unit,
    onContinue: () -> Unit,
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
                NonComplianceTextInputComponent(
                    modifier = Modifier.weight(1f),
                    textFieldValue = textFieldValue,
                    nonComplianceInputFieldState = textFieldState,
                    isInputValid = isInputValid,
                    placeholder = placeholder,
                    supportingText = supportingText,
                    onTextFieldValueChange = onTextChange,
                    onTrailingIcon = onClearInput,
                    onKeyboardAction = onContinue,
                    keyboardType = KeyboardType.Email,
                )

                Spacer(modifier = Modifier.width(Sizes.s04))

                Buttons.FilledPrimary(
                    text = stringResource(R.string.tk_global_continue),
                    enabled = isInputValid,
                    onClick = onContinue,
                )
            }
        }

        item { Spacer(modifier = Modifier.height(Sizes.s02)) }
    }
}
