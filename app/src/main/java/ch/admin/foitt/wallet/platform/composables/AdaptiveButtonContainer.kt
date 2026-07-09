package ch.admin.foitt.wallet.platform.composables

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Dp
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTheme

/**
 * Bottom button container with adaptive row/column layout.
 *
 * Row mode: buttons share available width equally. If any button's text exceeds its share, layout
 * automatically falls back to column. In row mode the first button appears on the right (primary
 * action position).
 *
 * Column mode (default): each button fills max width. Triggered by [stacked] or automatic overflow.
 *
 * An empty [buttons] list renders nothing.
 */
@Composable
fun AdaptiveBottomButtonBar(
    buttons: List<@Composable () -> Unit>,
    modifier: Modifier = Modifier,
    stacked: Boolean = true,
    horizontalGapSize: Dp = Sizes.s04,
    verticalGapSize: Dp = Sizes.s04,
    windowInsets: WindowInsets = NavigationBarDefaults.windowInsets,
    contentPadding: PaddingValues = PaddingValues(
        start = Sizes.s04,
        top = Sizes.s02,
        end = Sizes.s04,
        bottom = Sizes.s04
    ),
    backgroundColor: Color = WalletTheme.colorScheme.background.copy(
        alpha = if (isSystemInDarkTheme()) 0.8f else 0.85f
    ),
) {
    if (buttons.isNotEmpty()) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = Sizes.s06, topEnd = Sizes.s06),
            color = backgroundColor,
        ) {
            AdaptiveButtonContainer(
                buttons = buttons,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(windowInsets)
                    .padding(contentPadding),
                stacked = stacked,
                horizontalGapSize = horizontalGapSize,
                verticalGapSize = verticalGapSize,
            )
        }
    }
}

@Composable
fun AdaptiveButtonContainer(
    buttons: List<@Composable () -> Unit>,
    modifier: Modifier = Modifier,
    stacked: Boolean = true,
    horizontalGapSize: Dp = Sizes.s04,
    verticalGapSize: Dp = Sizes.s04,
) {
    if (buttons.isNotEmpty()) {
        SubcomposeLayout(
            modifier = modifier
        ) { constraints ->
            // Per-item keys (the button index) so a change to one button only recomposes
            // that slot rather than the whole content.
            val measurables = buttons.indices.flatMap { index ->
                subcompose(index) { buttons[index]() }
            }
            val count = measurables.size
            val horizontalGapPx = horizontalGapSize.roundToPx()
            val verticalGapPx = verticalGapSize.roundToPx()
            val totalWidth = constraints.maxWidth
            val widthEach = maxOf(0, (totalWidth - horizontalGapPx * (count - 1)) / count)

            val useColumn = stacked || count == 1 ||
                measurables.any { it.maxIntrinsicWidth(constraints.maxHeight) > widthEach }

            if (useColumn) {
                val colConstraints = constraints.copy(minWidth = totalWidth, maxWidth = totalWidth)
                val placeables = measurables.map { it.measure(colConstraints) }
                val contentHeight = placeables.sumOf { it.height } + verticalGapPx * (count - 1)
                layout(totalWidth, contentHeight) {
                    var y = 0
                    placeables.forEach { placeable ->
                        placeable.place(0, y)
                        y += placeable.height + verticalGapPx
                    }
                }
            } else {
                val rowConstraints = constraints.copy(minWidth = widthEach, maxWidth = widthEach)
                // reversed: first button appears on the right (primary action position)
                val placeables = measurables.reversed().map { it.measure(rowConstraints) }
                val maxHeight = placeables.maxOf { it.height }
                layout(totalWidth, maxHeight) {
                    var x = 0
                    placeables.forEach { placeable ->
                        placeable.place(x, 0)
                        x += widthEach + horizontalGapPx
                    }
                }
            }
        }
    }
}

@WalletAllScreenPreview
@Composable
private fun AdaptiveBottomButtonSinglePreview() {
    WalletTheme {
        Box(contentAlignment = Alignment.BottomCenter) {
            AdaptiveBottomButtonBar(
                buttons = listOf(
                    { Buttons.FilledPrimary(text = "single button", onClick = {}) },
                )
            )
        }
    }
}

@WalletAllScreenPreview
@Composable
private fun AdaptiveBottomButtonDoubleSideBySidePreview() {
    WalletTheme {
        Box(contentAlignment = Alignment.BottomCenter) {
            AdaptiveBottomButtonBar(
                stacked = false,
                buttons = listOf(
                    { Buttons.FilledPrimary(text = "primary button", onClick = {}) },
                    { Buttons.FilledSecondary(text = "secondary button", onClick = {}) },
                )
            )
        }
    }
}

@WalletAllScreenPreview
@Composable
private fun AdaptiveBottomButtonDoubleStackedOverflowPreview() {
    WalletTheme {
        Box(contentAlignment = Alignment.BottomCenter) {
            AdaptiveBottomButtonBar(
                stacked = false,
                buttons = listOf(
                    {
                        Buttons.FilledPrimary(
                            text = "primary button but with long text that does not fit side by side",
                            onClick = {},
                        )
                    },
                    { Buttons.FilledSecondary(text = "secondary button", onClick = {}) },
                )
            )
        }
    }
}

@WalletAllScreenPreview
@Composable
private fun AdaptiveBottomButtonDoubleStackedPreview() {
    WalletTheme {
        Box(contentAlignment = Alignment.BottomCenter) {
            AdaptiveBottomButtonBar(
                stacked = true,
                buttons = listOf(
                    { Buttons.FilledPrimary(text = "primary button", onClick = {}) },
                    { Buttons.FilledSecondary(text = "secondary button", onClick = {}) },
                )
            )
        }
    }
}
