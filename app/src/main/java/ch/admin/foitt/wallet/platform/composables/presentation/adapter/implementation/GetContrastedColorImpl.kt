package ch.admin.foitt.wallet.platform.composables.presentation.adapter.implementation

import android.content.Context
import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import ch.admin.foitt.wallet.platform.composables.presentation.adapter.GetContrastedColor
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class GetContrastedColorImpl @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
) : GetContrastedColor {
    override fun invoke(
        backgroundColor: Color,
        backgroundOverlayColor: Color,
        darkContentColor: Color,
        lightContentColor: Color,
    ): Color {
        val composedBackgroundColor = ColorUtils.compositeColors(
            backgroundOverlayColor.toArgb(),
            backgroundColor.toArgb(),
        )

        val opacityFilterColor =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && appContext.resources.configuration.isNightModeActive) {
                Color.Black
            } else {
                Color.White
            }
        val opaqueBackground = ColorUtils.compositeColors(composedBackgroundColor, opacityFilterColor.toArgb())

        val contrastWithBlack = ColorUtils.calculateContrast(darkContentColor.toArgb(), opaqueBackground)
        val contrastWithWhite = ColorUtils.calculateContrast(lightContentColor.toArgb(), opaqueBackground)

        return if (contrastWithWhite > contrastWithBlack) lightContentColor else darkContentColor
    }
}
