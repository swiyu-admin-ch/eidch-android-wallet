package ch.admin.foitt.wallet.platform.composables.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts

fun LazyListScope.clusterFooter(
    paddingValues: PaddingValues = PaddingValues(0.dp),
    @StringRes text: Int,
) = item {
    Spacer(modifier = Modifier.height(Sizes.s02))
    WalletTexts.LabelMedium(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = paddingValues.calculateStartPadding(LayoutDirection.Ltr) + Sizes.s04,
                end = paddingValues.calculateEndPadding(LayoutDirection.Ltr) + Sizes.s04,
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding(),
            ),
        text = stringResource(text),
    )
}
