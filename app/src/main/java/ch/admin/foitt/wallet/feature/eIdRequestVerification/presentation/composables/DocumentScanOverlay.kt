package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.model.DocumentTypeDrawable
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun DocumentScanOverlay(
    documentTypeDrawables: DocumentTypeDrawable,
    showBackside: Boolean,
    modifier: Modifier = Modifier,
    cardOrientation: Float = 0f,
) {
    val rotation = remember { Animatable(0f) }
    var currentOverlayRes by remember(documentTypeDrawables) {
        mutableIntStateOf(if (showBackside) documentTypeDrawables.back else documentTypeDrawables.front)
    }
    var isInitialComposition by remember { mutableStateOf(true) }

    LaunchedEffect(showBackside) {
        if (isInitialComposition) {
            isInitialComposition = false
            return@LaunchedEffect
        }
        val flipOut: suspend () -> Unit = {
            rotation.animateTo(
                targetValue = 90f,
                animationSpec = tween(durationMillis = 250, easing = LinearEasing)
            )
        }
        val flipIn: suspend () -> Unit = {
            rotation.snapTo(-90f)
            rotation.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 250, easing = LinearEasing)
            )
        }
        if (showBackside) {
            flipOut()
            currentOverlayRes = documentTypeDrawables.back
            flipIn()
        } else {
            flipOut()
            currentOverlayRes = documentTypeDrawables.front
            flipIn()
        }
    }

    Image(
        painter = painterResource(id = currentOverlayRes),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        colorFilter = ColorFilter.tint(WalletTheme.colorScheme.onPrimaryFixed),
        modifier = modifier
            .padding(start = Sizes.s03, end = Sizes.s03)
            .rotate(cardOrientation)
            .sizeIn(maxWidth = 1000.dp, maxHeight = 1000.dp)
            .graphicsLayer {
                rotationY = rotation.value
                cameraDistance = 8 * density
            }
    )
}
