package ch.admin.foitt.wallet.platform.composables.presentation

import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_MEDIUM_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND

enum class WindowWidthClass {
    COMPACT,
    MEDIUM,
    EXPANDED
}

enum class WindowHeightClass {
    COMPACT,
    MEDIUM,
    EXPANDED
}

fun WindowAdaptiveInfo.windowWidthClass(): WindowWidthClass {
    return when {
        this.windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND) -> WindowWidthClass.EXPANDED
        this.windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND) -> WindowWidthClass.MEDIUM
        else -> WindowWidthClass.COMPACT
    }
}

fun WindowAdaptiveInfo.windowHeightClass(): WindowHeightClass {
    return when {
        this.windowSizeClass.isHeightAtLeastBreakpoint(HEIGHT_DP_EXPANDED_LOWER_BOUND) -> WindowHeightClass.EXPANDED
        this.windowSizeClass.isHeightAtLeastBreakpoint(HEIGHT_DP_MEDIUM_LOWER_BOUND) -> WindowHeightClass.MEDIUM
        else -> WindowHeightClass.COMPACT
    }
}
