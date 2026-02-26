package ch.admin.foitt.wallet.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object Gradients {
    const val CREDENTIAL_GRADIENT_ALPHA_01 = 0.3f
    const val CREDENTIAL_GRADIENT_ALPHA_02 = 0.1f

    fun diagonalCredentialBrush() = Brush.linearGradient(
        0.0f to Color.Black.copy(alpha = CREDENTIAL_GRADIENT_ALPHA_01),
        1.0f to Color.Transparent
    )

    fun leftBottomRadialCredentialBrush(size: Size) = Brush.radialGradient(
        0.0f to Color.Black.copy(alpha = CREDENTIAL_GRADIENT_ALPHA_01),
        1.0f to Color.Transparent,
        center = Offset(x = 0.18f * size.width, y = 1.04f * size.height),
        radius = 0.5f * size.minDimension,
    )

    fun leftBottomRadialLargeCredentialBrush(size: Size) = Brush.radialGradient(
        0.0f to Color.Black.copy(alpha = CREDENTIAL_GRADIENT_ALPHA_01),
        1.0f to Color.Transparent,
        center = Offset(x = 0f, y = size.height),
        radius = 0.6f * size.width,
    )

    fun bottomCenterRadialCredentialBrush(size: Size) = Brush.radialGradient(
        0.0f to Color.Black.copy(alpha = CREDENTIAL_GRADIENT_ALPHA_02),
        1.0f to Color.Transparent,
        center = Offset(x = 0.55f * size.width, y = 0.9f * size.height),
        radius = 0.3f * size.minDimension,
    )
}
