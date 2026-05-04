package ch.admin.foitt.wallet.platform.composables.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun ClaimClusterCard(
    content: @Composable () -> Unit,
) = ClusterCard(
    modifier = Modifier.padding(start = Sizes.s04, end = Sizes.s04),
    colors = CardDefaults.cardColors().copy(containerColor = WalletTheme.colorScheme.listItemBackground),
) {
    content()
}

@Composable
fun InfoClusterCard(
    content: @Composable () -> Unit,
) = ClusterCard(
    modifier = Modifier.padding(horizontal = Sizes.s04),
) {
    content()
}

@Composable
private fun ClusterCard(
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.cardColors(),
    shape: RoundedCornerShape = RoundedCornerShape(Sizes.s05),
    content: @Composable () -> Unit,
) = Card(
    modifier = modifier,
    colors = colors,
    shape = shape,
) {
    content()
}
