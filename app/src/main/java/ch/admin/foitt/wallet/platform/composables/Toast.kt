package ch.admin.foitt.wallet.platform.composables

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.presentation.requestFocus
import ch.admin.foitt.wallet.platform.composables.presentation.spaceBarKeyClickable
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.platform.utils.TestTags
import ch.admin.foitt.wallet.platform.utils.TraversalIndex
import ch.admin.foitt.wallet.platform.utils.traversalIndex
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletShapes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun ScanInfoToast(
    modifier: Modifier = Modifier,
    shouldRequestFocus: Boolean = false,
    text: Int
) = Toast(
    modifier = modifier,
    modifierRow = Modifier,
    shouldRequestFocus = shouldRequestFocus,
    isSnackBarDesign = false,
    backgroundColor = WalletTheme.colorScheme.surface,
    iconStart = null,
    text = text,
    textColor = WalletTheme.colorScheme.onSurfaceVariant
)

@Composable
fun PassphraseValidationErrorToastFixed(
    modifier: Modifier = Modifier,
    shouldRequestFocus: Boolean = false,
    @StringRes text: Int = R.string.tk_global_warning_alt,
    @StringRes iconEndContentDescription: Int? = R.string.tk_global_closewarning_alt,
    onIconEnd: () -> Unit,
) = Toast(
    modifier = modifier,
    shouldRequestFocus = shouldRequestFocus,
    backgroundColor = WalletTheme.colorScheme.lightErrorFixed,
    iconStart = R.drawable.wallet_ic_warning,
    iconStartColor = WalletTheme.colorScheme.onLightErrorFixed,
    text = text,
    textColor = WalletTheme.colorScheme.onLightErrorFixed,
    iconEnd = R.drawable.wallet_ic_cross,
    iconEndColor = WalletTheme.colorScheme.onLightErrorFixed,
    iconEndContentDescription = iconEndContentDescription,
    onIconEnd = onIconEnd
)

@Composable
fun PassphraseValidationErrorToast(
    modifier: Modifier = Modifier,
    @StringRes text: Int = R.string.tk_global_warning_alt,
    @StringRes iconEndContentDescription: Int? = R.string.tk_global_closewarning_alt,
    onIconEnd: () -> Unit,
) = Toast(
    modifier = modifier,
    backgroundColor = WalletTheme.colorScheme.lightError,
    iconStart = R.drawable.wallet_ic_warning,
    iconStartColor = WalletTheme.colorScheme.onLightError,
    text = text,
    textColor = WalletTheme.colorScheme.onLightError,
    iconEnd = R.drawable.wallet_ic_cross,
    iconEndColor = WalletTheme.colorScheme.onLightError,
    iconEndContentDescription = iconEndContentDescription,
    onIconEnd = onIconEnd
)

@Composable
fun Toast(
    modifier: Modifier = Modifier,
    modifierRow: Modifier = Modifier.fillMaxWidth(),
    shouldRequestFocus: Boolean = false,
    isSnackBarDesign: Boolean = false,
    backgroundColor: Color = WalletTheme.colorScheme.surface,
    @StringRes headline: Int? = null,
    headlineColor: Color = WalletTheme.colorScheme.onSurface,
    @StringRes text: Int? = null,
    textAsString: String? = null,
    textColor: Color = WalletTheme.colorScheme.onSurfaceVariant,
    @StringRes linkText: Int? = null,
    @DrawableRes iconStart: Int? = null,
    iconStartColor: Color = WalletTheme.colorScheme.onSurfaceVariant,
    @DrawableRes iconEnd: Int? = null,
    iconEndColor: Color = WalletTheme.colorScheme.onSurfaceVariant,
    @StringRes iconEndContentDescription: Int? = null,
    onLink: () -> Unit = {},
    onIconEnd: () -> Unit = {},
) {
    val focusRequester = remember { FocusRequester() }

    Surface(
        modifier = modifier,
        shadowElevation = Sizes.line02,
        shape = if (isSnackBarDesign) WalletShapes.default.extraSmall else WalletShapes.default.large,
        color = backgroundColor,
    ) {
        Row(
            modifier = modifierRow
                .then(
                    if (isSnackBarDesign) {
                        Modifier.padding(start = Sizes.s04, top = Sizes.s01, bottom = Sizes.s01, end = Sizes.s01)
                    } else {
                        Modifier.padding(start = Sizes.s04, top = Sizes.s03, bottom = Sizes.s03, end = Sizes.s01)
                    }
                )
                .testTag(TestTags.ERROR.name)
                .semantics(mergeDescendants = true) {},
            verticalAlignment = Alignment.CenterVertically
        ) {
            iconStart?.let {
                Icon(
                    painter = painterResource(id = iconStart),
                    contentDescription = null,
                    tint = iconStartColor,
                )
                Spacer(modifier = Modifier.width(Sizes.s04))
            }
            Column(
                modifier = Modifier
                    .then(if (shouldRequestFocus) Modifier.requestFocus(focusRequester) else Modifier)
                    .weight(1f, fill = false)
            ) {
                headline?.let {
                    WalletTexts.TitleSmall(
                        text = stringResource(id = headline),
                        color = headlineColor,
                        modifier = Modifier.traversalIndex(TraversalIndex.HIGH1)
                    )
                }

                val displayText = textAsString ?: text?.let { stringResource(id = it) } ?: ""

                WalletTexts.LabelLarge(
                    text = displayText,
                    color = textColor,
                    modifier = Modifier.traversalIndex(TraversalIndex.HIGH2)
                )
                linkText?.let {
                    Spacer(modifier = Modifier.height(Sizes.s01))
                    Buttons.TextLink(
                        text = stringResource(id = linkText),
                        endIcon = painterResource(id = R.drawable.wallet_ic_chevron),
                        onClick = onLink,
                        modifier = Modifier.traversalIndex(TraversalIndex.HIGH3)
                    )
                }
            }
            Spacer(modifier = Modifier.width(Sizes.s03))
            iconEnd?.let {
                IconButton(
                    onClick = onIconEnd,
                    modifier = Modifier.spaceBarKeyClickable(onIconEnd),
                ) {
                    Icon(
                        modifier = Modifier
                            .clip(CircleShape)
                            .traversalIndex(TraversalIndex.LOW1),
                        painter = painterResource(id = iconEnd),
                        contentDescription = iconEndContentDescription?.let {
                            stringResource(iconEndContentDescription)
                        },
                        tint = iconEndColor,
                    )
                }
            }
        }
    }

    LaunchedEffect(shouldRequestFocus) {
        if (shouldRequestFocus) {
            focusRequester.requestFocus()
        }
    }
}

@WalletComponentPreview
@Composable
private fun ToastPreview() {
    WalletTheme {
        Toast(
            headline = R.string.tk_global_warning_alt,
            text = R.string.tk_onboarding_introductionStep_security_secondary,
            linkText = R.string.tk_global_warning_alt,
            iconStart = R.drawable.wallet_ic_qr,
            iconEnd = R.drawable.wallet_ic_cross,
            onLink = { },
            onIconEnd = { },
            shouldRequestFocus = false,
            isSnackBarDesign = false
        )
    }
}

@WalletComponentPreview
@Composable
private fun SnackbarDesignPreview() {
    WalletTheme {
        Toast(
            text = R.string.tk_home_notification_credential_declined,
            onIconEnd = { },
            isSnackBarDesign = true
        )
    }
}

@WalletComponentPreview
@Composable
private fun ErrorToastPreview() {
    WalletTheme {
        PassphraseValidationErrorToast(
            text = R.string.tk_global_warning_alt,
            onIconEnd = {},
        )
    }
}
