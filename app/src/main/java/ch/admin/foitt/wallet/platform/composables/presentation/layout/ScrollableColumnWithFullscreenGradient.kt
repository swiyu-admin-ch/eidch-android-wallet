package ch.admin.foitt.wallet.platform.composables.presentation.layout

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.presentation.WindowWidthClass
import ch.admin.foitt.wallet.platform.composables.presentation.windowWidthClass
import ch.admin.foitt.wallet.platform.scaffold.presentation.FullscreenGradient
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun WalletLayouts.ScrollableColumnWithFullscreenGradient(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    stickyBottomContent: (@Composable () -> Unit)?,
    scrollableContent: @Composable ColumnScope.() -> Unit,
) {
    FullscreenGradient()
    when (currentWindowAdaptiveInfo().windowWidthClass()) {
        WindowWidthClass.COMPACT -> CompactContainerFloatingBottom(
            modifier = modifier,
            content = {
                scrollableContent()
            },
            stickyBottomContent = {
                stickyBottomContent?.let {
                    stickyBottomContent()
                }
            }
        )
        else -> LargeContainerFloatingBottom(
            modifier = modifier,
            content = {
                scrollableContent()
            },
            stickyBottomContent = {
                stickyBottomContent?.let {
                    stickyBottomContent()
                }
            }
        )
    }

    LoadingOverlay(
        showOverlay = isLoading,
        color = WalletTheme.colorScheme.primaryFixed,
    )
}
