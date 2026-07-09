package ch.admin.foitt.wallet.platform.composables.presentation.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun WalletLayouts.LazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    useTopInsets: Boolean = true,
    useBottomInsets: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(
        bottom = paddingContentBottom,
    ),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlgnment: Alignment.Horizontal = Alignment.Start,
    lazyListContent: LazyListScope.() -> Unit,
) = androidx.compose.foundation.lazy.LazyColumn(
    modifier = modifier,
    state = state,
    contentPadding = contentPadding,
    verticalArrangement = verticalArrangement,
    horizontalAlignment = horizontalAlgnment
) {
    if (useTopInsets) {
        item {
            Spacer(
                Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            )
        }
    }
    lazyListContent()
    if (useBottomInsets) {
        item {
            Spacer(
                Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
            )
        }
    }
}
