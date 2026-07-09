package ch.admin.foitt.wallet.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

object WalletListItems {
    @Composable
    fun Cluster(
        isFirstItem: Boolean,
        isLastItem: Boolean,
        divider: (@Composable () -> Unit)? = ::Divider,
        paddingValues: PaddingValues = PaddingValues(),
        backgroundColor: Color = WalletTheme.colorScheme.listItemBackground,
        cornerSize: Dp = Sizes.s05,
        content: @Composable () -> Unit,
    ) = Box(
        modifier = Modifier
            .padding(paddingValues)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isFirstItem) {
                        Modifier.clip(RoundedCornerShape(topStart = cornerSize, topEnd = cornerSize))
                    } else {
                        Modifier
                    }
                )
                .then(
                    if (isLastItem) {
                        Modifier.clip(RoundedCornerShape(bottomStart = cornerSize, bottomEnd = cornerSize))
                    } else {
                        Modifier
                    }
                )
                .background(backgroundColor)
        ) {
            content()
            if (!isLastItem && divider != null) {
                divider()
            }
        }
    }

    @Composable
    fun Divider() = HorizontalDivider(
        modifier = Modifier
            .background(WalletTheme.colorScheme.listItemBackground)
            .padding(start = Sizes.s04),
        thickness = Sizes.line01,
        color = WalletTheme.colorScheme.outlineVariant,
    )

    @Composable
    fun CredentialDivider() = HorizontalDivider(
        thickness = Sizes.line01,
        color = WalletTheme.colorScheme.outlineVariant,
        modifier = Modifier
            .background(WalletTheme.colorScheme.listItemBackground)
            .padding(start = Sizes.s04 + Sizes.credentialVerySmallWidth + Sizes.s04)
    )
}
