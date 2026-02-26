@file:Suppress("TooManyFunctions")

package ch.admin.foitt.wallet.platform.composables

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.presentation.spaceBarKeyClickable
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletButtonColors
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

object Buttons {
    @Composable
    fun FilledPrimary(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        startIcon: Painter? = null,
        endIcon: Painter? = null,
        enabled: Boolean = true,
        isActive: Boolean = false,
        activeText: String? = text,
    ) = BaseButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        startIcon = startIcon,
        endIcon = endIcon,
        enabled = enabled,
        isActive = isActive,
        activeText = activeText,
        colors = WalletButtonColors.primary(),
    )

    @Composable
    fun FilledSecondary(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        startIcon: Painter? = null,
        endIcon: Painter? = null,
        enabled: Boolean = true,
    ) = BaseButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        startIcon = startIcon,
        endIcon = endIcon,
        enabled = enabled,
        isActive = false,
        colors = WalletButtonColors.secondary(),
    )

    @Composable
    fun FilledPrimaryFixed(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        startIcon: Painter? = null,
        endIcon: Painter? = null,
        enabled: Boolean = true,
    ) = BaseButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        startIcon = startIcon,
        endIcon = endIcon,
        enabled = enabled,
        colors = WalletButtonColors.primaryFixed(),
    )

    @Composable
    fun FilledSecondaryFixed(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        startIcon: Painter? = null,
        endIcon: Painter? = null,
        enabled: Boolean = true,
    ) = BaseButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        startIcon = startIcon,
        endIcon = endIcon,
        enabled = enabled,
        colors = WalletButtonColors.secondaryFixed(),
    )

    @Composable
    fun FilledSecondaryContainerFixed(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        startIcon: Painter? = null,
        endIcon: Painter? = null,
        enabled: Boolean = true,
    ) = BaseButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        startIcon = startIcon,
        endIcon = endIcon,
        enabled = enabled,
        colors = WalletButtonColors.secondaryContainerFixed(),
    )

    @Composable
    fun FilledTertiary(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        startIcon: Painter? = null,
        endIcon: Painter? = null,
        enabled: Boolean = true,
        isActive: Boolean = false,
        activeText: String? = text,
    ) = BaseButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        startIcon = startIcon,
        endIcon = endIcon,
        enabled = enabled,
        isActive = isActive,
        activeText = activeText,
        colors = WalletButtonColors.tertiary(),
    )

    @Composable
    fun Outlined(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        startIcon: Painter? = null,
        endIcon: Painter? = null,
        enabled: Boolean = true,
    ) = BaseButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        startIcon = startIcon,
        endIcon = endIcon,
        enabled = enabled,
        colors = WalletButtonColors.outlined(),
        hasBorder = true,
    )

    @Composable
    fun Text(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        startIcon: Painter? = null,
        endIcon: Painter? = null,
        enabled: Boolean = true,
        colors: ButtonColors = WalletButtonColors.text(),
    ) = BaseButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        startIcon = startIcon,
        endIcon = endIcon,
        enabled = enabled,
        colors = colors
    )

    @Composable
    fun TonalSecondary(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        startIcon: Painter? = null,
        endIcon: Painter? = null,
        enabled: Boolean = true,
    ) = BaseButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        startIcon = startIcon,
        endIcon = endIcon,
        enabled = enabled,
        colors = WalletButtonColors.tonal(),
    )

    @Composable
    fun TextLink(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        endIcon: Painter? = null,
    ) {
        val color = WalletTheme.colorScheme.error
        val linkAltText = stringResource(R.string.tk_global_externalLink_alt)
        Row(
            modifier = modifier
                .clickable(onClick = onClick)
                .spaceBarKeyClickable(onClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.Text(
                modifier = Modifier.semantics {
                    contentDescription = "$text $linkAltText"
                    role = Role.Button
                },
                text = text,
                color = color,
                style = WalletTheme.typography.bodySmall,
                textAlign = TextAlign.Start,
                overflow = TextOverflow.Ellipsis,
            )
            endIcon?.let {
                Icon(
                    modifier = Modifier
                        .size(Sizes.s03),
                    painter = endIcon,
                    tint = color,
                    contentDescription = null,
                )
            }
        }
    }

    @Composable
    fun Icon(
        @DrawableRes icon: Int,
        contentDescription: String,
        colors: IconButtonColors = WalletButtonColors.iconSecondaryFixed(),
        onClick: () -> Unit,
    ) = IconButton(
        modifier = Modifier.width(Sizes.s14),
        colors = colors,
        onClick = onClick,
    ) {
        Icon(
            modifier = Modifier.size(18.dp),
            painter = painterResource(icon),
            contentDescription = contentDescription
        )
    }
}

@Composable
private fun BaseButton(
    text: String,
    onClick: () -> Unit,
    colors: ButtonColors,
    modifier: Modifier = Modifier,
    startIcon: Painter? = null,
    endIcon: Painter? = null,
    enabled: Boolean = true,
    isActive: Boolean = false,
    activeText: String? = text,
    hasBorder: Boolean = false
) = Button(
    onClick = onClick,
    modifier = modifier.spaceBarKeyClickable(onClick),
    shape = WalletTheme.shapes.extraLarge,
    colors = colors,
    enabled = enabled,
    border = if (hasBorder) buttonBorder(enabled = enabled, buttonColors = colors) else null,
) {
    ButtonContent(
        text = text,
        startIcon = startIcon,
        endIcon = endIcon,
        isActive = isActive,
        activeText = activeText,
    )
}

@Composable
private fun ButtonContent(
    text: String,
    startIcon: Painter?,
    endIcon: Painter?,
    activeText: String? = text,
    isActive: Boolean = false,
) {
    if (isActive) {
        CircularProgressIndicator(
            color = LocalContentColor.current,
            strokeWidth = Sizes.line02,
            modifier = Modifier
                .width(Sizes.s04)
                .height(Sizes.s04)
                .focusable(false)
        )
        if (activeText != null) {
            Spacer(modifier = Modifier.width(Sizes.s02))
        }
    }
    startIcon?.let { icon ->
        if (!isActive) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(Sizes.buttonIcon)
                    .focusable(false)
            )
            Spacer(modifier = Modifier.width(Sizes.s02))
        }
    }
    if (isActive) {
        activeText?.let {
            WalletTexts.Button(text = activeText)
        }
    } else {
        WalletTexts.Button(text = text)
    }

    endIcon?.let { icon ->
        Spacer(modifier = Modifier.width(Sizes.s02))
        Icon(
            painter = icon,
            contentDescription = null,
            modifier = Modifier
                .size(Sizes.buttonIcon)
                .focusable(false)
        )
    }
}

@Composable
private fun buttonBorder(
    enabled: Boolean,
    buttonColors: ButtonColors,
) = BorderStroke(
    width = Sizes.line01,
    color = if (enabled) {
        WalletTheme.colorScheme.outline
    } else {
        buttonColors.disabledContentColor
    },
)

@WalletComponentPreview
@Composable
private fun BottomButtonPreview() {
    WalletTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(Sizes.s03)
        ) {
            Buttons.FilledPrimary(text = "Click Me Primary", onClick = {})
            Buttons.FilledPrimary(text = "Primary Disable", onClick = {}, enabled = false)
            Buttons.TonalSecondary(text = "Click Me Secondary", onClick = {})
            Buttons.TonalSecondary(text = "Secondary Disable", onClick = {}, enabled = false)
            Buttons.FilledTertiary(text = "Click Me Tertiary", onClick = {})
            Buttons.FilledTertiary(text = "Tertiary Disable", onClick = {}, enabled = false)
            Buttons.FilledTertiary(
                text = "Click Me with icon",
                onClick = {},
                startIcon = painterResource(id = R.drawable.wallet_ic_qr)
            )
            Buttons.FilledTertiary(
                text = "Click Me with icon",
                onClick = {},
                startIcon = painterResource(id = R.drawable.wallet_ic_external_link)
            )
            Buttons.FilledTertiary(
                text = "Click Me with icon and long text: Lorem ipsum dolor sit amet, consectetur adipiscing elit",
                onClick = {},
                startIcon = painterResource(id = R.drawable.wallet_ic_qr)
            )
            Buttons.Outlined(text = "Click Me Outlined", onClick = {})
            Buttons.Outlined(text = "Outlined disabled ", enabled = false, onClick = { })
            Buttons.FilledTertiary(text = "Click Me Disabled", onClick = {}, enabled = false)
            Buttons.FilledTertiary(
                text = "Loading Primary",
                onClick = {},
                isActive = true,
                startIcon = painterResource(id = R.drawable.wallet_ic_qr),
                activeText = "This button is loading..."
            )
            Buttons.FilledTertiary(
                text = "Loading Primary without loading text",
                onClick = {},
                isActive = true,
                activeText = null,
            )
            Buttons.Icon(
                icon = R.drawable.ic_fingerprint,
                contentDescription = "",
                onClick = {},
            )
            Buttons.Text(
                text = "Text button",
                onClick = {},
            )
        }
    }
}
