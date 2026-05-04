package ch.admin.foitt.wallet.feature.walletPairing.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.StandardErrorScreen
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
internal fun EIdWalletPairingTimeoutScreen(viewModel: EIdWalletPairingTimeoutViewModel) {
    BackHandler(onBack = viewModel::onClose)

    EIdWalletPairingTimeoutScreenContent(
        onClose = viewModel::onClose,
    )
}

@Composable
private fun EIdWalletPairingTimeoutScreenContent(
    onClose: () -> Unit,
) = WalletLayouts.StandardErrorScreen(
    mainImage = R.drawable.wallet_ic_queue_colored,
    primaryText = R.string.tk_eidRequest_walletPairing_timeout_primary,
    secondaryText = R.string.tk_eidRequest_walletPairing_timeout_secondary,
    primaryActionText = R.string.tk_eidRequest_walletPairing_timeout_button_primary,
    primaryAction = onClose,
)

@WalletAllScreenPreview
@Composable
private fun EIdWalletPairingTimeoutScreenPreview() {
    WalletTheme {
        EIdWalletPairingTimeoutScreenContent(
            onClose = {},
        )
    }
}
