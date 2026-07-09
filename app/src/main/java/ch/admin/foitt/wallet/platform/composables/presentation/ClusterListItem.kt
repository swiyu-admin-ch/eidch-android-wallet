package ch.admin.foitt.wallet.platform.composables.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import ch.admin.foitt.wallet.theme.WalletListItems

fun LazyListScope.clusterLazyListItem(
    isFirstItem: Boolean,
    isLastItem: Boolean,
    divider: (@Composable () -> Unit)? = WalletListItems::Divider,
    paddingValues: PaddingValues = PaddingValues(),
    content: @Composable () -> Unit,
) = item {
    WalletListItems.Cluster(
        isFirstItem = isFirstItem,
        isLastItem = isLastItem,
        divider = divider,
        paddingValues = paddingValues,
        content = content
    )
}
