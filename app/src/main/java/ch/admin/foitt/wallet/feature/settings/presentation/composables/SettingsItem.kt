@file:Suppress("TooManyFunctions")

package ch.admin.foitt.wallet.feature.settings.presentation.composables

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.presentation.spaceBarKeyClickable
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletListItems
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun WalletListItems.SectionHeadlineSettingsItem(
    title: String
) = SettingsItem(
    title = title,
    titleTextColor = WalletTheme.colorScheme.onSurface,
    titleIsHeadline = true,
    backgroundColor = WalletTheme.colorScheme.surfaceContainerLow,
)

@Composable
fun WalletListItems.LinkSettingsItem(
    title: String,
    onClick: () -> Unit,
    @DrawableRes leadingIcon: Int? = null,
) = SettingsItem(
    title = title,
    titleAltText = "$title ${stringResource(R.string.tk_global_externalLink_alt)}",
    backgroundColor = WalletTheme.colorScheme.listItemBackground,
    onClick = onClick,
    leadingContent = leadingIcon?.let {
        {
            Icon(
                painter = painterResource(id = it),
                contentDescription = null
            )
        }
    },
    trailingContent = {
        Icon(
            painter = painterResource(id = R.drawable.wallet_ic_external_link),
            contentDescription = null
        )
    },
)

@Composable
fun WalletListItems.SwitchSettingsItem(
    title: String,
    subtitle: String?,
    isSwitchEnabled: Boolean = true,
    isSwitchChecked: Boolean,
    onSwitchChange: (Boolean) -> Unit,
    @DrawableRes leadingIcon: Int? = null,
) = SettingsItem(
    title = title,
    subtitle = subtitle,
    leadingContent = leadingIcon?.let {
        {
            Icon(
                painter = painterResource(id = it),
                contentDescription = null
            )
        }
    },
    trailingContent = {
        Switch(
            enabled = isSwitchEnabled,
            checked = isSwitchChecked,
            onCheckedChange = onSwitchChange,
        )
    },
)

@Composable
fun WalletListItems.ClickableTextSettingsItem(
    title: String,
    onClick: () -> Unit,
    @DrawableRes leadingIcon: Int? = null,
) = SettingsItem(
    title = title,
    backgroundColor = WalletTheme.colorScheme.listItemBackground,
    onClick = onClick,
    leadingContent = leadingIcon?.let {
        {
            Icon(
                painter = painterResource(id = it),
                contentDescription = null
            )
        }
    },
    trailingContent = {
        Icon(
            painter = painterResource(id = R.drawable.wallet_ic_chevron_right),
            contentDescription = null
        )
    },
)

@Composable
fun WalletListItems.ButtonSettingsItem(
    title: String,
    onClick: () -> Unit,
    contentColor: Color = LocalContentColor.current,
    @DrawableRes leadingIcon: Int? = null,
) = SettingsItem(
    title = title,
    titleTextColor = contentColor,
    backgroundColor = WalletTheme.colorScheme.listItemBackground,
    onClick = onClick,
    leadingContent = leadingIcon?.let {
        {
            Icon(
                painter = painterResource(id = it),
                contentDescription = null,
                tint = contentColor,
            )
        }
    },
)

@Composable
fun WalletListItems.TextSettingsItem(
    title: String,
    subtitle: String? = null,
    leadingContent: @Composable (() -> Unit)? = null,
) = SettingsItem(
    title = title,
    subtitle = subtitle,
    leadingContent = leadingContent,
)

@Composable
fun WalletListItems.LicenseSettingsItem(
    title: String,
    version: String?,
    onLibraryClick: () -> Unit,
) = ListItem(
    modifier = Modifier
        .clickable(onClick = onLibraryClick)
        .spaceBarKeyClickable(onLibraryClick),
    colors = ListItemDefaults.colors(containerColor = WalletTheme.colorScheme.listItemBackground),
    headlineContent = {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WalletTexts.BodyLarge(
                modifier = Modifier.weight(1f),
                text = title,
                color = WalletTheme.colorScheme.onSurface,
            )
            version?.let {
                WalletTexts.LabelSmall(
                    text = it,
                    color = WalletTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    },
    trailingContent = {
        Icon(
            painter = painterResource(id = R.drawable.wallet_ic_chevron_right),
            contentDescription = null,
        )
    },
)

@Composable
fun WalletListItems.VersionSettingsItem(
    title: String,
    version: String,
) = SettingsItem(
    title = title,
    trailingContent = {
        WalletTexts.LabelSmall(
            text = version,
            color = WalletTheme.colorScheme.onSurfaceVariant,
        )
    }
)

@Composable
fun WalletListItems.SpecialLinkSettingsItem(
    title: String,
    onClick: () -> Unit,
) = ListItem(
    modifier = Modifier
        .clickable(onClick = onClick)
        .spaceBarKeyClickable(onClick),
    colors = ListItemDefaults.colors(containerColor = WalletTheme.colorScheme.listItemBackground),
    headlineContent = {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val linkAltText = stringResource(R.string.tk_global_externalLink_alt)
            WalletTexts.BodyMedium(
                modifier = Modifier.semantics {
                    contentDescription = "$title $linkAltText"
                    role = Role.Button
                },
                text = title,
                color = WalletTheme.colorScheme.error,
            )
            Icon(
                painter = painterResource(R.drawable.wallet_ic_chevron_medium),
                contentDescription = null,
                tint = WalletTheme.colorScheme.error,
            )
        }
    },
)

@Composable
fun WalletListItems.LanguageSettingsItem(
    title: String,
    isChecked: Boolean,
    onLanguageClick: () -> Unit
) = SettingsItem(
    title = title,
    onClick = onLanguageClick,
    trailingContent = {
        if (isChecked) {
            Icon(
                painter = painterResource(id = R.drawable.wallet_ic_checkmark_big),
                contentDescription = stringResource(R.string.tk_menu_language_android_selected_language),
            )
        }
    }
)

@Composable
private fun SettingsItem(
    title: String,
    titleTextColor: Color = WalletTheme.colorScheme.onSurface,
    titleIsHeadline: Boolean = false,
    titleAltText: String = title,
    subtitle: String? = null,
    backgroundColor: Color = WalletTheme.colorScheme.listItemBackground,
    onClick: (() -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) = ListItem(
    modifier = Modifier
        .then(
            if (onClick == null) {
                Modifier
            } else {
                Modifier
                    .clickable(onClick = onClick)
                    .spaceBarKeyClickable(onClick)
            }
        )
        .then(
            if (titleIsHeadline) {
                Modifier.semantics {
                    heading()
                }
            } else {
                Modifier
            }
        ),
    colors = ListItemDefaults.colors(containerColor = backgroundColor),
    headlineContent = {
        // This is a workaround of a material3.ListItem bug wrt TalkBack where trailingContent is read before headlineContent.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WalletTexts.BodyLarge(
                modifier = Modifier.semantics {
                    contentDescription = titleAltText
                },
                text = title,
                color = titleTextColor,
            )
            trailingContent?.invoke()
        }
    },
    supportingContent = subtitle?.let {
        {
            WalletTexts.BodyMedium(
                text = it,
                color = WalletTheme.colorScheme.onSurfaceVariant,
            )
        }
    },
    leadingContent = leadingContent,
)

@WalletComponentPreview
@Composable
fun SettingsItemPreview() {
    WalletTheme {
        SettingsItem(
            title = "Feedback geben",
            onClick = {}
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(Sizes.s02)
        ) {
            WalletListItems.SectionHeadlineSettingsItem(
                title = "Headline",
            )
            WalletListItems.LinkSettingsItem(
                title = "Item with link to external",
                onClick = {},
            )
            WalletListItems.SwitchSettingsItem(
                title = "Item with switch",
                subtitle = "Subtitle",
                isSwitchEnabled = true,
                isSwitchChecked = true,
                onSwitchChange = {},
            )
            WalletListItems.ClickableTextSettingsItem(
                title = "Item clickable text",
                onClick = {},
            )
            WalletListItems.ButtonSettingsItem(
                title = "Button",
                onClick = {},
            )
            WalletListItems.TextSettingsItem(
                title = "Item only title",
                subtitle = "subtitle",
            )
            WalletListItems.LicenseSettingsItem(
                title = "Library name",
                version = "1.2.3",
                onLibraryClick = {},
            )
            WalletListItems.VersionSettingsItem(
                title = "Build number",
                version = "1.2.3"
            )
            WalletListItems.LanguageSettingsItem(
                title = "English",
                isChecked = true,
                onLanguageClick = {},
            )
            WalletListItems.SpecialLinkSettingsItem(
                title = "special link",
                onClick = {},
            )
        }
    }
}
