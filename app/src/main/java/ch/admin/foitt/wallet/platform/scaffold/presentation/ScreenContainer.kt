package ch.admin.foitt.wallet.platform.scaffold.presentation

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component1
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component2
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import ch.admin.foitt.wallet.platform.utils.TraversalIndex
import ch.admin.foitt.wallet.platform.utils.setIsTraversalGroup
import ch.admin.foitt.wallet.theme.WalletTheme
import timber.log.Timber

@Composable
fun ScreenContainer(
    content: @Composable BoxScope.() -> Unit,
) {
    val (focus01Ref, focus02Ref) = FocusRequester.createRefs()

    Scaffold(
        modifier = Modifier
            .setIsTraversalGroup(),
        topBar = {
            Box(
                modifier = Modifier
                    .setIsTraversalGroup(index = TraversalIndex.FIRST)
                    .focusGroup()
                    .focusRequester(focus02Ref)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
            ) {
                WalletTopBar()
            }
        },
        content = { scaffoldPaddings ->
            val modifier = Modifier
                .setIsTraversalGroup(index = TraversalIndex.HIGH5)
                .focusGroup()
                .focusRequester(focus01Ref)
                .focusProperties { next = focus02Ref }
                .fillMaxSize()

            CompositionLocalProvider(LocalScaffoldPaddings provides scaffoldPaddings) {
                Box(
                    modifier = modifier,
                ) {
                    content()
                }
            }
        },
        containerColor = WalletTheme.colorScheme.background,
    )
}

val LocalScaffoldPaddings = staticCompositionLocalOf<PaddingValues> {
    Timber.d(message = "No ScaffoldPaddings provided")
    PaddingValues(0.dp)
}
