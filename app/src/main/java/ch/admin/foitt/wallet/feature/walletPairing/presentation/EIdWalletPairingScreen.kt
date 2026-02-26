package ch.admin.foitt.wallet.feature.walletPairing.presentation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.walletPairing.presentation.model.WalletPairingUiState
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.StandardErrorScreen
import ch.admin.foitt.wallet.platform.composables.presentation.ScreenMainImage
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
internal fun EIdWalletPairingScreen(
    viewModel: EIdWalletPairingViewModel,
) {
    EIdWalletPairingScreenContent(
        uiState = viewModel.uiState.collectAsStateWithLifecycle().value,
        isLoading = viewModel.isLoading.collectAsStateWithLifecycle().value,
        onSingleDeviceFlow = viewModel::onSingleDeviceFlow,
        onMultiDeviceFlow = viewModel::onMultiDeviceFlow,
        onCloseError = viewModel::onCloseError,
    )
}

@Composable
private fun EIdWalletPairingScreenContent(
    uiState: WalletPairingUiState,
    onSingleDeviceFlow: () -> Unit,
    onMultiDeviceFlow: () -> Unit,
    onCloseError: () -> Unit,
    isLoading: Boolean,
) {
    when (uiState) {
        WalletPairingUiState.Initial -> InitialContent(
            isLoading = isLoading,
            onSingleDeviceFlow = onSingleDeviceFlow,
            onMultiDeviceFlow = onMultiDeviceFlow,
        )
        WalletPairingUiState.NetworkError -> NetworkErrorContent(
            onCloseError = onCloseError,
        )
        WalletPairingUiState.Unexpected -> UnexpectedErrorContent(
            onCloseError = onCloseError,
        )
    }
}

@Composable
private fun InitialContent(
    isLoading: Boolean,
    onSingleDeviceFlow: () -> Unit,
    onMultiDeviceFlow: () -> Unit,
) = WalletLayouts.ScrollableColumnWithPicture(
    stickyStartContent = {
        ScreenMainImage(
            iconRes = R.drawable.wallet_ic_pairing_colored,
            backgroundColor = WalletTheme.colorScheme.surfaceContainerLow,
        )
    },
    stickyBottomBackgroundColor = Color.Transparent,
    stickyBottomContent = {
        Buttons.TonalSecondary(
            text = stringResource(R.string.tk_getEid_walletPairing1_secondaryButton),
            onClick = onMultiDeviceFlow,
            enabled = !isLoading,
        )
        Buttons.FilledPrimary(
            text = stringResource(R.string.tk_getEid_walletPairing1_primaryButton),
            onClick = onSingleDeviceFlow,
            enabled = !isLoading,
            isActive = isLoading,
        )
    }
) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleScreen(
        text = stringResource(R.string.tk_getEid_walletPairing1_title)
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.BodyLarge(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(R.string.tk_getEid_walletPairing1_body)
    )
}

@Composable
private fun UnexpectedErrorContent(
    onCloseError: () -> Unit,
) = WalletLayouts.StandardErrorScreen(
    primaryText = R.string.tk_getEid_walletPairing1_unexpectedError_primary,
    secondaryText = R.string.tk_getEid_walletPairing1_unexpectedError_secondary,
    primaryActionText = R.string.tk_getEid_walletPairing1_unexpectedError_button_closeError,
    primaryAction = onCloseError,
)

@Composable
private fun NetworkErrorContent(
    onCloseError: () -> Unit,
) = WalletLayouts.StandardErrorScreen(
    primaryText = R.string.tk_getEid_walletPairing1_networkError_primary,
    secondaryText = R.string.tk_getEid_walletPairing1_networkError_secondary,
    primaryActionText = R.string.tk_getEid_walletPairing1_networkError_button_closeError,
    primaryAction = onCloseError,
)

//region Preview
private class EIdWalletPairingScreenPreviewParams : PreviewParameterProvider<Boolean> {
    override val values: Sequence<Boolean> = sequenceOf(false, true)
}

@WalletAllScreenPreview
@Composable
private fun EIdWalletPairingScreenPreview(
    @PreviewParameter(EIdWalletPairingScreenPreviewParams::class) isLoading: Boolean
) {
    WalletTheme {
        EIdWalletPairingScreenContent(
            uiState = WalletPairingUiState.Initial,
            onSingleDeviceFlow = {},
            onMultiDeviceFlow = {},
            onCloseError = {},
            isLoading = isLoading,
        )
    }
}
//endregion
