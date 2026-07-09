package ch.admin.foitt.wallet.platform.composables

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.platform.utils.contentDescription
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun HorizontalButtonList(
    onButtonClicked: (Int) -> Unit,
    modifier: Modifier = Modifier,
    elevation: Dp = 0.dp,
    content: HorizontalButtonItemScope.() -> Unit,
) {
    Surface(
        color = WalletTheme.colorScheme.listItemBackground,
        shape = RoundedCornerShape(Sizes.s12),
        shadowElevation = elevation,
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Sizes.s02),
            modifier = Modifier.padding(Sizes.s03),
        ) {
            val scopeContent = ButtonListItemScopeImpl(0).apply(content)
            scopeContent.items.forEachIndexed { idx, item ->
                item.itemContent(idx) { onButtonClicked(idx) }
            }
        }
    }
}

@Composable
fun VerticalButtonList(
    onButtonClicked: (Int) -> Unit,
    modifier: Modifier = Modifier,
    elevation: Dp = 0.dp,
    content: VerticalButtonListItemScope.() -> Unit,
) {
    Surface(
        color = WalletTheme.colorScheme.listItemBackground,
        shape = RoundedCornerShape(Sizes.s12),
        shadowElevation = elevation,
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Sizes.s02),
            modifier = Modifier.padding(Sizes.s03),
        ) {
            val scopeContent = ButtonListItemScopeImpl(0).apply(content)
            scopeContent.items.forEachIndexed { idx, item ->
                item.itemContent(idx) { onButtonClicked(idx) }
            }
        }
    }
}

@ButtonListItemScopeMarker
interface HorizontalButtonItemScope {
    fun button(
        @DrawableRes iconId: Int,
        @StringRes label: Int? = null,
        @StringRes contentDescription: Int,
    )
}

@ButtonListItemScopeMarker
interface VerticalButtonListItemScope {
    fun button(
        @DrawableRes iconId: Int,
        @StringRes contentDescription: Int,
    )
}

@DslMarker
private annotation class ButtonListItemScopeMarker

private data class ButtonListItem(val itemContent: @Composable (idx: Int, onClick: () -> Unit) -> Unit)

private class ButtonListItemScopeImpl(
    private val primaryIndex: Int,
) : HorizontalButtonItemScope, VerticalButtonListItemScope {
    val items = mutableListOf<ButtonListItem>()

    override fun button(@DrawableRes iconId: Int, @StringRes label: Int?, @StringRes contentDescription: Int) {
        items.add(
            ButtonListItem { idx, onClick ->
                ButtonListButton(
                    icon = iconId,
                    text = label,
                    contentDescription = contentDescription,
                    isPrimary = primaryIndex == idx,
                    onClick = onClick,
                )
            }
        )
    }

    override fun button(@DrawableRes iconId: Int, @StringRes contentDescription: Int) {
        items.add(
            ButtonListItem { idx, onClick ->
                ButtonListButton(
                    icon = iconId,
                    text = null,
                    contentDescription = contentDescription,
                    isPrimary = primaryIndex == idx,
                    onClick = onClick,
                )
            }
        )
    }
}

@Composable
private fun ButtonListButton(
    @DrawableRes icon: Int,
    @StringRes text: Int?,
    @StringRes contentDescription: Int,
    isPrimary: Boolean,
    onClick: () -> Unit,
) {
    val containerColor by animateColorAsState(
        targetValue = if (isPrimary) WalletTheme.colorScheme.primary else WalletTheme.colorScheme.secondaryContainer,
    )
    val contentColor by animateColorAsState(
        targetValue = if (isPrimary) WalletTheme.colorScheme.onPrimary else WalletTheme.colorScheme.onSecondaryContainer,
    )

    val horizontalPadding = text?.let { Sizes.s06 } ?: Sizes.s04
    Button(
        onClick = onClick,
        contentPadding = PaddingValues(vertical = Sizes.s04, horizontal = horizontalPadding),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        shape = CircleShape,
        modifier = Modifier
            .sizeIn(minWidth = Sizes.s16, minHeight = Sizes.s16)
            .contentDescription(stringResource(contentDescription))
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(Sizes.s06),
            )
            text?.let {
                Spacer(modifier = Modifier.width(Sizes.s02))
                WalletTexts.BodyLargeEmphasized(text = stringResource(it), color = Color.Unspecified)
            }
        }
    }
}

@WalletComponentPreview
@Composable
private fun HorizontalButtonListPreview() {
    WalletTheme {
        HorizontalButtonList(onButtonClicked = { }) {
            button(R.drawable.wallet_ic_scan, R.string.tk_home_scan_button, R.string.tk_home_scan_button)
            button(R.drawable.wallet_ic_qr, contentDescription = R.string.qr_scanner_code_tab)
        }
    }
}

@WalletComponentPreview
@Composable
private fun VerticalButtonListPreview() {
    WalletTheme {
        VerticalButtonList(onButtonClicked = { }) {
            button(R.drawable.wallet_ic_scan, R.string.tk_home_scan_button)
            button(R.drawable.wallet_ic_qr, R.string.qr_scanner_code_tab)
        }
    }
}
