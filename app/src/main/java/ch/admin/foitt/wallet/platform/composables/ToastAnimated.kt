package ch.admin.foitt.wallet.platform.composables

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import ch.admin.foitt.wallet.platform.composables.presentation.horizontalSafeDrawing
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun ToastAnimated(
    isVisible: Boolean,
    @DrawableRes iconEnd: Int? = null,
    isSnackBarDesign: Boolean,
    contentBottomPadding: Dp? = null,
    @StringRes messageToast: Int?,
    onCloseToast: () -> Unit = {},
) = Box(
    modifier = Modifier
        .fillMaxSize()
        .horizontalSafeDrawing(),
) {
    AnimatedVisibility(
        visible = isVisible,
        modifier = Modifier.align(Alignment.BottomCenter),
        label = "infoToast",
        enter = slideInVertically(
            animationSpec = spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = IntOffset.VisibilityThreshold,
            ),
            initialOffsetY = { fullHeight -> fullHeight },
        ),
        exit = slideOutVertically(
            animationSpec = spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = IntOffset.VisibilityThreshold,
            ),
            targetOffsetY = { fullHeight -> fullHeight },
        )
    ) {
        val text = remember(this) { messageToast }
        if (text != null) {
            val bottomPadding = contentBottomPadding
                ?: (WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom).asPaddingValues().calculateBottomPadding() + Sizes.s06)
            Row(
                modifier = Modifier
                    .padding(start = Sizes.s08, end = Sizes.s08, bottom = bottomPadding)
            ) {
                Toast(
                    modifier = Modifier,
                    shouldRequestFocus = true,
                    text = text,
                    backgroundColor = WalletTheme.colorScheme.inverseSurface,
                    textColor = WalletTheme.colorScheme.inverseOnSurface,
                    iconEndColor = WalletTheme.colorScheme.inverseOnSurface,
                    iconEnd = iconEnd,
                    onIconEnd = onCloseToast,
                    isSnackBarDesign = isSnackBarDesign
                )
            }
        }
    }
}
