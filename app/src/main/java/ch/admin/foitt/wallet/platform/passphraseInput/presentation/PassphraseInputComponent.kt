package ch.admin.foitt.wallet.platform.passphraseInput.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.WalletTextField
import ch.admin.foitt.wallet.platform.passphraseInput.domain.model.PassphraseInputFieldState
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.platform.utils.TestTags
import ch.admin.foitt.wallet.theme.WalletTextFieldColors
import ch.admin.foitt.wallet.theme.WalletTheme
import kotlin.math.roundToInt
import kotlin.math.sin

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PassphraseInputComponent(
    modifier: Modifier = Modifier,
    passphraseInputFieldState: PassphraseInputFieldState = PassphraseInputFieldState.Typing,
    textFieldValue: TextFieldValue,
    enabled: Boolean = true,
    colors: TextFieldColors = WalletTextFieldColors.textFieldColors(),
    keyboardImeAction: ImeAction = ImeAction.Go,
    onKeyboardAction: () -> Unit,
    label: (@Composable () -> Unit)? = null,
    placeholder: @Composable () -> Unit = {},
    supportingText: @Composable () -> Unit = {},
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onAnimationFinished: (Boolean) -> Unit,
) {
    var showPassphrase by remember { mutableStateOf(false) }

    val errorAnimatable = createErrorAnimatable(
        passphraseInputFieldState = passphraseInputFieldState,
        onAnimationFinished = { onAnimationFinished(false) }
    )

    WalletTextField.TextInputField(
        modifier = modifier
            .offset {
                createShakingOffset(amplitude = 10.dp.roundToPx(), errorAnimatable = errorAnimatable)
            }
            .testTag(TestTags.PIN_FIELD.name),
        textFieldValue = textFieldValue,
        enabled = enabled,
        singleLine = true,
        isError = passphraseInputFieldState is PassphraseInputFieldState.Error,
        colors = colors,
        trailingIcon = {
            Icon(
                modifier = Modifier
                    .clickable {
                        showPassphrase = !showPassphrase
                    }
                    .testTag(TestTags.SHOW_PASSPHRASE_ICON.name),
                painter = if (showPassphrase) {
                    painterResource(R.drawable.wallet_ic_eye)
                } else {
                    painterResource(R.drawable.wallet_ic_eye_crossed)
                },
                contentDescription = if (showPassphrase) {
                    stringResource(R.string.tk_global_visible_alt)
                } else {
                    stringResource(R.string.tk_global_invisible_alt)
                }
            )
        },
        visualTransformation = if (showPassphrase) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardOptions = KeyboardOptions(
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Password,
            imeAction = keyboardImeAction,
        ),
        keyboardActions = when (keyboardImeAction) {
            ImeAction.Go -> KeyboardActions(onGo = { onKeyboardAction() })
            ImeAction.Next -> KeyboardActions(onNext = { onKeyboardAction() })
            else -> KeyboardActions()
        },
        label = label,
        placeholder = placeholder,
        supportingText = supportingText,
        onTextFieldValueChange = onTextFieldValueChange,
    )
}

@Composable
private fun createErrorAnimatable(
    passphraseInputFieldState: PassphraseInputFieldState,
    onAnimationFinished: () -> Unit
): Animatable<Float, AnimationVector1D> {
    val animation = remember { Animatable(0f) }
    LaunchedEffect(passphraseInputFieldState) {
        if (passphraseInputFieldState is PassphraseInputFieldState.Error) {
            animation.snapTo(0f)
            animation.animateTo(
                targetValue = 1f,
                animationSpec = tween(1000),
            )
            onAnimationFinished()
        }
    }
    return animation
}

private fun createShakingOffset(amplitude: Int, errorAnimatable: Animatable<Float, AnimationVector1D>) =
    IntOffset(
        x = (amplitude * sin(errorAnimatable.value * Math.PI * 3f).toFloat()).roundToInt(),
        y = 0
    )

@WalletComponentPreview
@Composable
private fun PassphraseInputComponentPreview() {
    WalletTheme {
        PassphraseInputComponent(
            textFieldValue = TextFieldValue("abc123"),
            onKeyboardAction = {},
            onTextFieldValueChange = {},
            onAnimationFinished = {},
        )
    }
}
