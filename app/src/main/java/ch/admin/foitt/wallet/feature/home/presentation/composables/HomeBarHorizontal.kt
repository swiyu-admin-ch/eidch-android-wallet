package ch.admin.foitt.wallet.feature.home.presentation.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Constraints
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.presentation.spaceBarKeyClickable
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.platform.utils.TestTags
import ch.admin.foitt.wallet.platform.utils.TraversalIndex
import ch.admin.foitt.wallet.platform.utils.contentDescription
import ch.admin.foitt.wallet.platform.utils.traversalIndex
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletButtonColors
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun HomeBarHorizontal(
    onScan: () -> Unit,
    onMenu: () -> Unit,
    usesBigScanButton: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) = Row(
    horizontalArrangement = Arrangement.spacedBy(
        space = Sizes.s02,
        alignment = Alignment.CenterHorizontally
    ),
    modifier = modifier
        .clip(RoundedCornerShape(corner = CornerSize(Sizes.s16)))
        .background(WalletTheme.colorScheme.background)
        .padding(Sizes.s03)
) {
    MenuIconButton(
        onClick = onMenu,
        modifier = Modifier.traversalIndex(TraversalIndex.HIGH2)
    )
    ScanButton(
        usesBigScanButton = usesBigScanButton,
        onClick = onScan,
        modifier = Modifier.traversalIndex(TraversalIndex.HIGH1)
    )
}

@Composable
private fun SubComposeScanButton(
    content: @Composable (canPlaceContent: Boolean) -> Unit
) {
    var canPlaceTextButton by remember {
        mutableStateOf(false)
    }
    SubcomposeLayout { constraints ->
        val buttonConstrained = subcompose("Button1") {
            ScanTextButton(onClick = {})
        }[0].measure(constraints)

        val buttonUnconstrained = subcompose("Button2") {
            ScanTextButton(onClick = {})
        }[0].measure(constraints.copy(maxWidth = Constraints.Infinity))

        canPlaceTextButton = buttonUnconstrained.width <= buttonConstrained.width
        // We are not interested in laying out manually.
        layout(0, 0) {}
    }
    content(canPlaceTextButton)
}

@Composable
fun HomeBarVertical(
    onScan: () -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier = Modifier,
) = Column(
    verticalArrangement = Arrangement.spacedBy(
        space = Sizes.s02,
        alignment = Alignment.CenterVertically,
    ),
    modifier = modifier
        .clip(RoundedCornerShape(corner = CornerSize(Sizes.s16)))
        .background(WalletTheme.colorScheme.background)
        .padding(Sizes.s03)
) {
    MenuIconButton(
        onClick = onMenu,
        modifier = Modifier.traversalIndex(TraversalIndex.HIGH2)
    )
    ScanIconButton(
        onClick = onScan,
        modifier = Modifier.traversalIndex(TraversalIndex.HIGH1)
    )
}

@Composable
private fun MenuIconButton(
    onClick: () -> Unit,
    modifier: Modifier,
) = Button(
    onClick = onClick,
    colors = WalletButtonColors.tonal(),
    contentPadding = PaddingValues(),
    modifier = modifier
        .size(Sizes.s16)
        .contentDescription(stringResource(id = R.string.tk_home_menu_button_altText))
        .spaceBarKeyClickable(onClick)
        .testTag(TestTags.MENU_BUTTON.name)
) {
    Icon(
        painter = painterResource(id = R.drawable.wallet_ic_menu),
        contentDescription = null,
    )
}

@Composable
private fun ScanButton(
    usesBigScanButton: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) = Box {
    SubComposeScanButton { canPlaceContent: Boolean ->
        usesBigScanButton(canPlaceContent)
        if (canPlaceContent) {
            ScanTextButton(
                onClick = onClick,
                modifier = modifier.fillMaxWidth().testTag(TestTags.SCAN_TEXT_BUTTON.name)
            )
        } else {
            ScanIconButton(
                onClick = onClick,
                modifier = modifier.testTag(TestTags.SCAN_TEXT_BUTTON.name),
            )
        }
    }
}

@Composable
private fun ScanIconButton(
    onClick: () -> Unit,
    modifier: Modifier,
) = Button(
    onClick = onClick,
    colors = WalletButtonColors.primary(),
    contentPadding = PaddingValues(),
    modifier = modifier
        .size(Sizes.s16)
        .contentDescription(stringResource(id = R.string.tk_home_scan_button_altText))
        .spaceBarKeyClickable(onClick)
) {
    Icon(
        painter = painterResource(id = R.drawable.wallet_ic_qr),
        contentDescription = null,
    )
}

@Composable
private fun ScanTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) = Button(
    onClick = onClick,
    shape = WalletTheme.shapes.extraLarge,
    colors = WalletButtonColors.primary(),
    contentPadding = PaddingValues(horizontal = Sizes.s08, vertical = Sizes.s04),
    modifier = modifier
        .height(Sizes.s16)
        .contentDescription(stringResource(id = R.string.tk_home_scan_button_altText))
        .spaceBarKeyClickable(onClick)
) {
    Icon(
        painter = painterResource(id = R.drawable.wallet_ic_qr),
        contentDescription = null,
    )
    Spacer(modifier = Modifier.width(Sizes.s02))

    WalletTexts.Button(
        text = stringResource(id = R.string.tk_home_scan_button)
    )
}

@WalletComponentPreview
@Composable
private fun HomeBarHorizontalPreview() {
    WalletTheme {
        HomeBarHorizontal(
            onMenu = {},
            onScan = {},
            usesBigScanButton = {},
        )
    }
}

@WalletComponentPreview
@Composable
private fun HomeBarVerticalPreview() {
    WalletTheme {
        HomeBarVertical(
            onMenu = {},
            onScan = {},
        )
    }
}
