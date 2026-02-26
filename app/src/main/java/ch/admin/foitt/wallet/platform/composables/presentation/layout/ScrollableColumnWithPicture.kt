package ch.admin.foitt.wallet.platform.composables.presentation.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ch.admin.foitt.wallet.platform.composables.ScalableContentLayout
import ch.admin.foitt.wallet.platform.composables.presentation.WindowWidthClass
import ch.admin.foitt.wallet.platform.composables.presentation.windowWidthClass
import ch.admin.foitt.wallet.platform.scaffold.presentation.LocalScaffoldPaddings
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTheme

/**
 * Standard Wallet Layout that wraps content in a scrollable [Column].
 * It handles:
 * * the scrolling behavior, including the various paddings
 * * the orientation changes
 * * the various insets (the status, navigation and sticky contents)
 */
@Composable
fun WalletLayouts.ScrollableColumnWithPicture(
    modifier: Modifier = Modifier,
    stickyBottomBackgroundColor: Color = WalletTheme.colorScheme.surface.copy(alpha = 0.85f),
    stickyBottomContent: @Composable (RowScope.() -> Unit)?,
    stickyStartContent: @Composable ColumnScope.() -> Unit,
    scaffoldPaddings: PaddingValues = LocalScaffoldPaddings.current,
    contentPadding: PaddingValues = PaddingValues(
        start = Sizes.s04,
        end = Sizes.s04,
        bottom = Sizes.s06
    ),
    content: @Composable ColumnScope.() -> Unit,
) {
    var bottomBlockHeightDp by remember {
        mutableStateOf(Sizes.s00)
    }

    val stickyBottomHorizontalArrangement = Arrangement.spacedBy(Sizes.s02, Alignment.End)
    val contentScrollState = rememberScrollState()

    when (currentWindowAdaptiveInfo().windowWidthClass()) {
        WindowWidthClass.COMPACT -> CompactContainer(
            modifier = modifier,
            stickyBottomHorizontalArrangement = stickyBottomHorizontalArrangement,
            stickyBottomBackgroundColor = stickyBottomBackgroundColor,
            stickyBottomContent = stickyBottomContent,
            onBottomHeightMeasured = { height -> bottomBlockHeightDp = height },
            scrollState = contentScrollState,
            shouldScrollUnderTopBar = true,
            scaffoldPaddings = scaffoldPaddings,
        ) { boxWithConstraintScope ->
            val verticalInsets = scaffoldPaddings.calculateBottomPadding() + scaffoldPaddings.calculateTopPadding()
            ScalableContentLayout(
                height = boxWithConstraintScope.maxHeight,
                layoutInsetHeight = verticalInsets,
                scalableContentIndex = 0,
                minScalableHeight = Sizes.mainCardMinHeight,
                maxScalableHeight = boxWithConstraintScope.maxHeight * cardLargeScreenRatio,
                stickyContentHeight = bottomBlockHeightDp,
            ) {
                stickyStartContent()
                Column(
                    modifier = Modifier.padding(contentPadding)
                ) {
                    content()
                }
            }
        }

        else -> LargeContainer(
            onBottomHeightMeasured = { height -> bottomBlockHeightDp = height },
            isStickyStartScrollable = false,
            stickyBottomHorizontalArrangement = stickyBottomHorizontalArrangement,
            stickyBottomBackgroundColor = stickyBottomBackgroundColor,
            stickyBottomContent = stickyBottomContent,
            stickyStartContent = stickyStartContent,
            contentScrollState = contentScrollState,
            contentPadding = contentPadding,
            scaffoldPaddings = scaffoldPaddings,
        ) {
            content()
        }
    }
}
