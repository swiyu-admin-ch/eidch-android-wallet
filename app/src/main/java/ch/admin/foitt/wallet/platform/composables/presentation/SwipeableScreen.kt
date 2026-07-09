package ch.admin.foitt.wallet.platform.composables.presentation

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue

/**
 * Wrapper that detects an horizontal swipe gesture.
 * Triggers [onSwipeForward] or [onSwipeBackWard] callbacks depending on the swipe direction.
 */
@Composable
fun SwipeableScreen(
    onSwipeForward: () -> Unit,
    onSwipeBackWard: () -> Unit,
    ratioToSwipe: Float = 0.20f,
    content: @Composable () -> Unit
) {
    val currentScreenWidth: Dp = LocalConfiguration.current.screenWidthDp.dp
    var totalOffset: Float by remember {
        mutableFloatStateOf(0f)
    }
    Box(
        modifier = Modifier
            .focusGroup()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        totalOffset = 0f
                    },
                    onDragEnd = {
                        if ((totalOffset / ratioToSwipe).absoluteValue.dp > currentScreenWidth) {
                            if (totalOffset < 0f) {
                                onSwipeForward()
                            } else {
                                onSwipeBackWard()
                            }
                        }
                    },

                ) { change, dragAmount ->
                    totalOffset += dragAmount
                    change.consume()
                }
            }
    ) {
        content()
    }
}
