package ch.admin.foitt.wallet.platform.eIdApplicationProcess.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.presentation.layout.LazyColumn
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.ApplyRequest
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.presentation.model.MrzData
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.utils.setIsTraversalGroup
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletListItems
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.annotation.Destination

@Destination
@Composable
fun MrzChooserScreen(
    viewModel: MrzChooserViewModel
) {
    val errorMessage = viewModel.errorMessage.collectAsStateWithLifecycle().value
    val showErrorMessage = viewModel.showErrorDialog.collectAsStateWithLifecycle().value

    MrzChooserScreenContent(
        errorMessage = errorMessage,
        screenData = viewModel.mrzData,
        onMrzItemClick = viewModel::onMrzItemClick,
        onCloseDialog = viewModel::onCloseErrorDialog,
        showErrorMessage = showErrorMessage,
    )
}

@Composable
private fun MrzChooserScreenContent(
    errorMessage: String,
    showErrorMessage: Boolean,
    screenData: List<MrzData>,
    onMrzItemClick: (Int) -> Unit,
    onCloseDialog: () -> Unit,
) {
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
                trailingIcon = R.drawable.pilot_ic_settings_next,
            )
        }
    }

    if (showErrorMessage) {
        ErrorDialog(
            onConfirmation = onCloseDialog,
            dialogText = errorMessage
        )
    }
}

@Composable
fun ErrorDialog(
    onConfirmation: () -> Unit,
    dialogText: String,
) {
    AlertDialog(
        title = {
            Text(text = "Error")
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onConfirmation()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text("OK")
            }
        },
    )
}

@WalletAllScreenPreview
@Composable
fun MrzScreenPreview() {
    WalletTheme {
        MrzChooserScreenContent(
            screenData = listOf(
                MrzData(
                    displayName = "Adult (ID-CARD)",
                    payload = ApplyRequest(
                        mrz = listOf(),
                    )
                ),
                MrzData(
                    displayName = "Adult (PASSPORT)",
                    payload = ApplyRequest(
                        mrz = listOf(),
                    )
                ),
                MrzData(
                    displayName = "Underage (ID-CARD)",
                    payload = ApplyRequest(
                        mrz = listOf(),
                    )
                )
            ),
            onMrzItemClick = {},
            errorMessage = "error message",
            showErrorMessage = false,
            onCloseDialog = {},
        )
    }
}
