package ch.admin.foitt.wallet.platform.composables.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalInspectionMode
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.platform.utils.generateQRBitmap
import ch.admin.foitt.wallet.theme.WalletTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

@Composable
fun QrCodeImage(
    content: String?,
    modifier: Modifier = Modifier,
) {
    val bitmap = rememberQrCodeBitmap(content)

    AnimatedContent(
        targetState = bitmap.value,
        transitionSpec = {
            fadeIn(animationSpec = tween(220, delayMillis = 90)) togetherWith fadeOut(animationSpec = tween(90))
        },
        modifier = modifier
    ) { bitmap ->
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
            )
        } else {
            Box(
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun rememberQrCodeBitmap(content: String?): State<ImageBitmap?> {
    return if (LocalInspectionMode.current) {
        // Render synchronously for the inspection preview
        remember(content) { mutableStateOf(content?.generateQRBitmap()) }
    } else {
        // Render asynchronously in normal mode
        val initial: ImageBitmap? = null
        produceState(initialValue = initial, key1 = content) {
            // Produce the bitmap state on the IO dispatcher because rendering can take quite a few milliseconds
            withContext(Dispatchers.IO) {
                val bitmap = content?.generateQRBitmap()
                ensureActive()
                value = bitmap
            }
        }
    }
}

@WalletComponentPreview
@Composable
private fun QrCodeImagePreview() {
    WalletTheme {
        QrCodeImage(content = "Swiyu Wallet")
    }
}
