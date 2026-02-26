package ch.admin.foitt.wallet.platform.composables

import android.view.inputmethod.EditorInfo
import androidx.annotation.DoNotInline
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.InterceptPlatformTextInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.PlatformTextInputMethodRequest
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import ch.admin.foitt.wallet.platform.utils.isScreenReaderOn
import ch.admin.foitt.wallet.theme.WalletTextFieldColors
import timber.log.Timber

object WalletTextField {
    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun TextInputField(
        modifier: Modifier,
        textFieldValue: TextFieldValue,
        isError: Boolean,
        onTextFieldValueChange: (TextFieldValue) -> Unit,
        readOnly: Boolean = false,
        enabled: Boolean = true,
        singleLine: Boolean = false,
        colors: TextFieldColors = WalletTextFieldColors.textFieldColors(),
        visualTransformation: VisualTransformation = VisualTransformation.None,
        keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
        keyboardActions: KeyboardActions = KeyboardActions.Default,
        interactionSource: MutableInteractionSource? = null,
        label: @Composable (() -> Unit)? = null,
        placeholder: @Composable (() -> Unit)? = null,
        supportingText: @Composable (() -> Unit)? = null,
        trailingIcon: @Composable (() -> Unit)? = null,
    ) {
        // Workaround to set https://developer.android.com/reference/android/view/inputmethod/EditorInfo#IME_FLAG_NO_PERSONALIZED_LEARNING flag
        // Source: https://issuetracker.google.com/issues/359257538
        InterceptPlatformTextInput(
            interceptor = { request, nextHandler ->
                val modifiedRequest = PlatformTextInputMethodRequest { outAttributes ->
                    request.createInputConnection(outAttributes).also {
                        NoPersonalizedLearningHelper.addNoPersonalizedLearning(outAttributes)
                    }
                }
                nextHandler.startInputMethod(modifiedRequest)
            }
        ) {
            val focusRequester = remember { FocusRequester() }

            if (enabled && !readOnly) {
                val windowInfo = LocalWindowInfo.current
                val keyboard = LocalSoftwareKeyboardController.current

                val context = LocalContext.current
                if (!context.isScreenReaderOn()) {
                    LaunchedEffect(windowInfo) {
                        snapshotFlow { windowInfo.isWindowFocused }.collect { isWindowFocused ->
                            if (isWindowFocused) {
                                focusRequester.requestFocus()
                                keyboard?.show() ?: Timber.w("InputField: keyboard not controllable")
                            }
                        }
                    }
                }
            }

            TextField(
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            onTextFieldValueChange(
                                TextFieldValue(
                                    text = textFieldValue.text,
                                    selection = TextRange(textFieldValue.text.length, textFieldValue.text.length)
                                )
                            )
                        }
                    }
                    .then(modifier),
                value = textFieldValue,
                isError = isError,
                onValueChange = onTextFieldValueChange,
                readOnly = readOnly,
                enabled = enabled,
                singleLine = singleLine,
                colors = colors,
                visualTransformation = visualTransformation,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                interactionSource = interactionSource,
                label = label,
                placeholder = placeholder,
                supportingText = supportingText,
                trailingIcon = trailingIcon,
            )
        }
    }

    internal object NoPersonalizedLearningHelper {
        @DoNotInline
        fun addNoPersonalizedLearning(info: EditorInfo) {
            info.imeOptions = info.imeOptions or EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING
        }
    }
}
