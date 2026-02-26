package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables

import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.viewinterop.AndroidView
import ch.admin.foitt.wallet.theme.Sizes
import timber.log.Timber

// AvBeam video has 16/9 fixed aspect ratio in landscape, then add black bars
private const val VideoAspectRatioLandscape = 1.77778f

// AvBeam video has 9/16 fixed aspect ratio in portrait, then add black bars
private const val VideoAspectRatioPortrait = 0.5625f

@Composable
internal fun ScannerCamera(
    containerWidth: Int,
    containerHeight: Int,
    getScannerView: suspend (width: Int, height: Int) -> View,
    onAfterViewLayout: (width: Int, height: Int) -> Unit,
) {
    var surfaceView by remember { mutableStateOf<View?>(null) }

    val surfaceViewContentScale by remember {
        val containerAspectRatio = containerWidth.toFloat() / containerHeight.toFloat()

        val videoScaling = when {
            containerAspectRatio > 1 -> computeCroppedScaling(
                containerAspectRatio = containerAspectRatio,
                videoAspectRatio = VideoAspectRatioLandscape,
                containerWidth = containerWidth,
                containerHeight = containerHeight,
            )
            else -> computeCroppedScaling(
                containerAspectRatio = containerAspectRatio,
                videoAspectRatio = VideoAspectRatioPortrait,
                containerWidth = containerWidth,
                containerHeight = containerHeight,
            )
        }
        mutableFloatStateOf(videoScaling)
    }

    LaunchedEffect(containerWidth, containerHeight) {
        surfaceView = getScannerView(
            (containerWidth * surfaceViewContentScale).toInt(),
            (containerHeight * surfaceViewContentScale).toInt(),
        )
    }

    val currentSurfaceView = surfaceView ?: return

    AndroidView(
        factory = {
            currentSurfaceView.detachFromParent()
            currentSurfaceView
        },
        update = { surfaceView ->
            surfaceView.post {
                onAfterViewLayout(surfaceView.width, surfaceView.height)
            }
            Timber.d("Recording: AndroidView updated")
        },
        onReset = null,
        modifier = Modifier
            .fillMaxSize()
            .clip(
                RoundedCornerShape(
                    topStart = Sizes.boxCornerSize,
                    topEnd = Sizes.boxCornerSize,
                )
            )
            .graphicsLayer(
                scaleX = surfaceViewContentScale,
                scaleY = surfaceViewContentScale,
                clip = true,
            )
    )
}

private fun View.detachFromParent() {
    (parent as? ViewGroup)?.removeView(this)
}

private fun computeCroppedScaling(
    containerAspectRatio: Float,
    videoAspectRatio: Float,
    containerWidth: Int,
    containerHeight: Int,
): Float = if (containerAspectRatio >= videoAspectRatio) {
    containerWidth / (containerHeight * videoAspectRatio)
} else {
    containerHeight / (containerWidth / videoAspectRatio)
}
