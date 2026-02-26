package ch.admin.foitt.wallet.feature.home.presentation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.presentation.spaceBarKeyClickable
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletShapes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun HomeCompactMenu(
    preferredWidth: Dp,
    showEIdRequestButton: Boolean,
    showBetaIdRequestButton: Boolean,
    onMenu: (Boolean) -> Unit,
    onGetEId: () -> Unit,
    onGetBetaId: () -> Unit,
    onSettings: () -> Unit,
    onHelp: () -> Unit,
) = Popup(
    alignment = Alignment.BottomStart,
    onDismissRequest = { onMenu(false) },
    properties = PopupProperties(
        focusable = true,
        dismissOnBackPress = true,
        dismissOnClickOutside = true,
    ),
) {
    HomeMenuContent(
        preferredWidth = preferredWidth,
        showEIdRequestButton = showEIdRequestButton,
        showBetaIdRequestButton = showBetaIdRequestButton,
        onGetEId = onGetEId,
        onGetBetaId = onGetBetaId,
        onSettings = onSettings,
        onHelp = onHelp,
    )
}

@Composable
fun HomeLargeMenu(
    preferredWidth: Dp,
    showEIdRequestButton: Boolean,
    showBetaIdRequestButton: Boolean,
    onMenu: (Boolean) -> Unit,
    onGetEId: () -> Unit,
    onGetBetaId: () -> Unit,
    onSettings: () -> Unit,
    onHelp: () -> Unit,
) {
    val density = LocalDensity.current
    val xOffset = with(density) {
        Sizes.s04.toPx().toInt()
    }

    Popup(
        alignment = Alignment.CenterStart,
        offset = IntOffset(-xOffset, 0),
        onDismissRequest = { onMenu(false) },
        properties = PopupProperties(
            focusable = true,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        HomeMenuContent(
            preferredWidth = preferredWidth,
            showEIdRequestButton = showEIdRequestButton,
            showBetaIdRequestButton = showBetaIdRequestButton,
            onGetEId = onGetEId,
            onGetBetaId = onGetBetaId,
            onSettings = onSettings,
            onHelp = onHelp,
        )
    }
}

@Composable
private fun HomeMenuContent(
    preferredWidth: Dp,
    showEIdRequestButton: Boolean,
    showBetaIdRequestButton: Boolean,
    onGetEId: () -> Unit,
    onGetBetaId: () -> Unit,
    onSettings: () -> Unit,
    onHelp: () -> Unit,
) = Column(
    modifier = Modifier
        .widthIn(Sizes.homeMenuMinWidth, preferredWidth)
        .clip(WalletShapes.default.large)
        .verticalScroll(rememberScrollState())
) {
    if (showEIdRequestButton) {
        MenuItem(
            title = stringResource(R.string.tk_menu_homeList_orderEid),
            leadingIcon = R.drawable.wallet_ic_credential,
            onClick = onGetEId,
        )
    }
    if (showBetaIdRequestButton) {
        MenuItem(
            title = stringResource(R.string.tk_menu_homeList_menu_add),
            leadingIcon = R.drawable.wallet_ic_credential,
            onClick = onGetBetaId,
        )
    }
    MenuItem(
        title = stringResource(R.string.tk_menu_homeList_settings),
        leadingIcon = R.drawable.wallet_ic_settings,
        onClick = onSettings,
    )
    val helpTitle = stringResource(R.string.tk_menu_homeList_help)
    val helpLinkAltText = stringResource(R.string.tk_global_externalLink_alt)
    MenuItem(
        title = helpTitle,
        titleAltText = "$helpTitle $helpLinkAltText",
        leadingIcon = R.drawable.wallet_ic_questionmark,
        trailingIcon = R.drawable.wallet_ic_external_link,
        onClick = onHelp,
    )
}

@Composable
private fun MenuItem(
    title: String,
    titleAltText: String = title,
    @DrawableRes leadingIcon: Int,
    @DrawableRes trailingIcon: Int = R.drawable.wallet_ic_chevron_right,
    onClick: () -> Unit,
) = ListItem(
    modifier = Modifier
        .clickable(onClick = onClick)
        .spaceBarKeyClickable(onClick),
    colors = ListItemDefaults.colors(containerColor = WalletTheme.colorScheme.surfaceContainerHighest),
    headlineContent = {
        WalletTexts.BodyLarge(
            modifier = Modifier
                .wrapContentWidth()
                .semantics {
                    contentDescription = titleAltText
                },
            text = title,
            color = WalletTheme.colorScheme.onSurfaceVariant,
        )
    },
    leadingContent = {
        Icon(
            painter = painterResource(id = leadingIcon),
            contentDescription = null,
            tint = WalletTheme.colorScheme.onSurfaceVariant,
        )
    },
    trailingContent = {
        Icon(
            painter = painterResource(id = trailingIcon),
            contentDescription = null,
            tint = WalletTheme.colorScheme.onSurfaceVariant,
        )
    }
)
