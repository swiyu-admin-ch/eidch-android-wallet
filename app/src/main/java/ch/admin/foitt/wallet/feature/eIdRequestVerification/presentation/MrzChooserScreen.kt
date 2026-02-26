package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.presentation.layout.LazyColumn
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.TestMrzData
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.scaffold.presentation.LocalScaffoldPaddings
import ch.admin.foitt.wallet.platform.utils.setIsTraversalGroup
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletListItems
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun MrzChooserScreen(
    viewModel: MrzChooserViewModel
) {
    MrzChooserScreenContent(
        screenData = viewModel.mrzData,
        onMrzItemClick = viewModel::onMrzItemClick,
    )
}

@Composable
private fun MrzChooserScreenContent(
    screenData: List<TestMrzData>,
    onMrzItemClick: (Int) -> Unit,
) {
    Column {
        Spacer(
            modifier = Modifier.padding(
                top = LocalScaffoldPaddings.current.calculateTopPadding()
            )
        )
        WalletLayouts.LazyColumn(
            useBottomInsets = false,
            modifier = Modifier
                .setIsTraversalGroup()
                .fillMaxHeight(),
            contentPadding = PaddingValues(
                top = Sizes.s06,
                bottom = Sizes.s06
            )
        ) {
            itemsIndexed(screenData) { index, state ->
                WalletListItems.SimpleListItem(
                    leadingIcon = R.drawable.wallet_ic_account,
                    title = "${index + 1} ${state.displayName}",
                    onItemClick = { onMrzItemClick(index) },
                    trailingIcon = R.drawable.wallet_ic_chevron,
                )
            }
            item {
                Spacer(
                    modifier = Modifier.padding(
                        top = LocalScaffoldPaddings.current.calculateBottomPadding()
                    )
                )
            }
        }
    }
}

@WalletAllScreenPreview
@Composable
private fun MrzScreenPreview() {
    WalletTheme {
        MrzChooserScreenContent(
            screenData = listOf(
                TestMrzData(
                    displayName = "Adult (ID-CARD)",
                    mrz = listOf(),
                ),
                TestMrzData(
                    displayName = "Adult (PASSPORT)",
                    mrz = listOf(),
                ),
                TestMrzData(
                    displayName = "Underage (ID-CARD)",
                    mrz = listOf(),
                )
            ),
            onMrzItemClick = {},
        )
    }
}
