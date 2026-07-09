package ch.admin.foitt.wallet.platform.composables.presentation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import androidx.compose.ui.util.fastCoerceIn
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.theme.Gradients
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTheme
import kotlinx.coroutines.delay

private val LinearIndicatorWidth = 240.dp
private val LinearIndicatorHeight = 4.dp

/**
 * A custom implementation of [androidx.compose.material3.LinearProgressIndicator] that supports a gradient indicator color.
 * While a big chunk of the implementation is copied this implements the Wallet design:
 * - default no gap
 * - no stop indicator
 * - custom default stroke caps
 */
@ExperimentalMaterial3Api
@Composable
fun WalletLinearProgressIndicator(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    brush: Brush = Gradients.linearProgressIndicatorBrush(),
    trackColor: Color = WalletTheme.colorScheme.progressTrackColor,
) {
    val coercedProgress = { progress().fastCoerceIn(0f, 1f) }

    Canvas(
        modifier = modifier
            .then(IncreaseVerticalSemanticsBounds)
            .semantics {
                // Check for NaN, as the ProgressBarRangeInfo will throw an exception.
                progressBarRangeInfo =
                    ProgressBarRangeInfo(coercedProgress().takeUnless { it.isNaN() } ?: 0f, 0f..1f)
            }
            .size(LinearIndicatorWidth, LinearIndicatorHeight)
            .clip(RoundedCornerShape(Sizes.cornerLarge))
    ) {
        val strokeWidth = size.height
        val currentCoercedProgress = coercedProgress()

        // track
        if (currentCoercedProgress < 1f) {
            drawLinearIndicator(currentCoercedProgress, 1f, SolidColor(trackColor), strokeWidth)
        }

        // indicator
        drawLinearIndicator(0f, currentCoercedProgress, brush, strokeWidth)
    }
}

private fun DrawScope.drawLinearIndicator(
    startFraction: Float,
    endFraction: Float,
    brush: Brush,
    strokeWidth: Float,
) {
    val width = size.width
    val height = size.height
    // Start drawing from the vertical center of the stroke
    val yOffset = height / 2

    val isLtr = layoutDirection == LayoutDirection.Ltr
    val barStart = (if (isLtr) startFraction else 1f - endFraction) * width
    val barEnd = (if (isLtr) endFraction else 1f - startFraction) * width

    drawLine(brush, Offset(barStart, yOffset), Offset(barEnd, yOffset), strokeWidth)
}

private val VerticalSemanticsBoundsPadding: Dp = 10.dp

private val IncreaseVerticalSemanticsBounds: Modifier =
    Modifier
        .layout { measurable, constraints ->
            val paddingPx = VerticalSemanticsBoundsPadding.roundToPx()
            // We need to add vertical padding to the semantics bounds in order to meet
            // screenreader green box minimum size, but we also want to
            // preserve a visual appearance and layout size below that minimum
            // in order to maintain backwards compatibility. This custom
            // layout effectively implements "negative padding".
            val newConstraint = constraints.offset(0, paddingPx * 2)
            val placeable = measurable.measure(newConstraint)

            // But when actually placing the placeable, create the layout without additional
            // space. Place the placeable where it would've been without any extra padding.
            val height = placeable.height - paddingPx * 2
            val width = placeable.width
            layout(width, height) { placeable.place(0, -paddingPx) }
        }
        .semantics(mergeDescendants = true) {}
        .padding(vertical = VerticalSemanticsBoundsPadding)

@OptIn(ExperimentalMaterial3Api::class)
@WalletComponentPreview
@Composable
private fun WalletLinearProgressIndicatorPreview() {
    WalletTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            WalletLinearProgressIndicator(
                progress = { 0.0f },
            )
            WalletLinearProgressIndicator(
                progress = { 0.5f },
            )
            WalletLinearProgressIndicator(
                progress = { 1.0f },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@WalletComponentPreview
@Composable
private fun WalletLinearProgressIndicatorAnimatedPreview() {
    val steps = listOf(0f, 0.25f, 0.5f, 0.75f, 1f)
    var stepIndex by remember { mutableIntStateOf(0) }
    val progress by animateFloatAsState(
        targetValue = steps[stepIndex],
        animationSpec = tween(durationMillis = 500, easing = LinearEasing),
        label = "progress",
    )
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            stepIndex = (stepIndex + 1) % steps.size
        }
    }
    WalletTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            WalletLinearProgressIndicator(
                progress = { progress },
            )
        }
    }
}
