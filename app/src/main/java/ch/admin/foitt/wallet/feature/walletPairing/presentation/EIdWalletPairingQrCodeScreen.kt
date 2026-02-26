package ch.admin.foitt.wallet.feature.walletPairing.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.model.QrBoxUiState
import ch.admin.foitt.wallet.feature.walletPairing.presentation.model.WalletPairingQrCodeUiState
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.NetworkErrorContent
import ch.admin.foitt.wallet.platform.composables.StandardErrorScreen
import ch.admin.foitt.wallet.platform.composables.UnexpectedErrorContent
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.utils.OnPauseEventHandler
import ch.admin.foitt.wallet.platform.utils.OnResumeEventHandler
import ch.admin.foitt.wallet.platform.utils.generateQRBitmap
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
internal fun EIdWalletPairingQrCodeScreen(
    viewModel: EIdWalletPairingQrCodeViewModel,
) {
    OnResumeEventHandler(viewModel::onResume)
    OnPauseEventHandler(viewModel::onPause)

    EIdWalletPairingQrCodeScreenContent(
        screenState = viewModel.screenUiState.collectAsStateWithLifecycle().value,
        qrBoxState = viewModel.qrBoxState.collectAsStateWithLifecycle().value,
        onRefresh = viewModel::onResume,
        onClose = viewModel::onBack,
    )
}

@Composable
private fun EIdWalletPairingQrCodeScreenContent(
    screenState: WalletPairingQrCodeUiState,
    qrBoxState: QrBoxUiState,
    onRefresh: () -> Unit,
    onClose: () -> Unit,
) {
    when (screenState) {
        WalletPairingQrCodeUiState.Done,
        WalletPairingQrCodeUiState.LoadingInvitation,
        WalletPairingQrCodeUiState.LoadingInvitationError,
        WalletPairingQrCodeUiState.PairingTimeout -> WalletPairingContent(
            qrBoxState = qrBoxState,
            waitingForDevicePairing = false,
            onRefresh = onRefresh,
            onCloseScreen = onClose,
        )
        WalletPairingQrCodeUiState.Polling -> WalletPairingContent(
            qrBoxState = qrBoxState,
            waitingForDevicePairing = true,
            onRefresh = onRefresh,
            onCloseScreen = onClose,
        )
        WalletPairingQrCodeUiState.NetworkError -> WalletLayouts.NetworkErrorContent(
            onCloseError = onClose,
            onRetry = onRefresh,
        )
        WalletPairingQrCodeUiState.PairingFailure -> WalletPairingRejectedContent(
            onRetry = onRefresh,
        )
        WalletPairingQrCodeUiState.UnexpectedError -> WalletLayouts.UnexpectedErrorContent(
            onCloseError = onClose,
        )
    }
}

@Composable
private fun WalletPairingContent(
    qrBoxState: QrBoxUiState,
    waitingForDevicePairing: Boolean,
    onRefresh: () -> Unit,
    onCloseScreen: () -> Unit,
) = WalletLayouts.ScrollableColumnWithPicture(
    stickyStartContent = {
        QrBox(
            state = qrBoxState,
            onRefresh = onRefresh,
        )
    },
    stickyBottomBackgroundColor = Color.Transparent,
    stickyBottomContent = {
        if (waitingForDevicePairing) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(WalletTheme.shapes.extraLarge)
                        .background(WalletTheme.colorScheme.background)
                        .padding(horizontal = Sizes.s04, vertical = Sizes.s02)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .semantics(mergeDescendants = true) {}
                    ) {
                        CircularProgressIndicator(
                            color = WalletTheme.colorScheme.primary,
                            trackColor = WalletTheme.colorScheme.secondaryContainer,
                            strokeWidth = Sizes.s01,
                            strokeCap = StrokeCap.Round,
                            modifier = Modifier.size(Sizes.s05)
                                .clearAndSetSemantics {}
                        )
                        Spacer(modifier = Modifier.width(Sizes.s02))
                        WalletTexts.BodyLarge(
                            text = stringResource(id = R.string.tk_walletPairing_devicePairingQRCode_fetchDevice_body),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(Sizes.s06))
        }
        Buttons.FilledPrimary(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.tk_walletPairing_devicePairingQRCode_button_close),
            onClick = onCloseScreen,
            enabled = qrBoxState !is QrBoxUiState.Loading,
        )
    },
) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleScreen(
        text = stringResource(id = R.string.tk_walletPairing_devicePairingQRCode_primary),
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.BodyLarge(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(id = R.string.tk_walletPairing_devicePairingQRCode_secondary),
    )
}

@Composable
private fun WalletPairingRejectedContent(
    onRetry: () -> Unit
) = WalletLayouts.StandardErrorScreen(
    primaryText = R.string.tk_eidRequest_walletPairing_offer_rejected_primary,
    secondaryText = R.string.tk_eidRequest_walletPairing_offer_rejected_secondary,
    primaryActionText = R.string.tk_eidRequest_walletPairing_offer_rejected_button_primary,
    primaryAction = onRetry,
    tertiaryActionText = R.string.tk_eidRequest_walletPairing_offer_rejected_tertiary,
)

@Composable
private fun QrBox(
    state: QrBoxUiState,
    onRefresh: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .clip(WalletTheme.shapes.extraLarge)
            .background(WalletTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = Sizes.s06)

    ) {
        when (state) {
            QrBoxUiState.Failure -> QrBoxFailure(onRefresh)
            QrBoxUiState.Loading -> QrBoxLoading()
            is QrBoxUiState.Success -> QrBoxSuccess(qrBitmap = state.qrBitmap)
        }
    }
}

@Composable
private fun QrBoxSuccess(
    qrBitmap: ImageBitmap,
) {
    Image(
        bitmap = qrBitmap,
        contentDescription = stringResource(R.string.tk_walletPairing_devicePairingQRCode_qr_alt),
        colorFilter = ColorFilter.tint(WalletTheme.colorScheme.onSurface),
        modifier = Modifier.fillMaxWidth(0.67f),
    )
}

@Composable
private fun QrBoxLoading() {
    CircularProgressIndicator(
        color = WalletTheme.colorScheme.primary,
        trackColor = WalletTheme.colorScheme.secondaryContainer,
        strokeWidth = Sizes.s01,
        strokeCap = StrokeCap.Round,
        modifier = Modifier.size(Sizes.s12)
    )
}

@Composable
private fun QrBoxFailure(
    onRefresh: () -> Unit,
) = Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
        .fillMaxWidth(0.67f)
        .verticalScroll(rememberScrollState())
) {
    Spacer(modifier = Modifier.height(Sizes.s02))
    Icon(
        painter = painterResource(R.drawable.wallet_ic_wrong_data),
        contentDescription = null,
        tint = WalletTheme.colorScheme.onSurface,
        modifier = Modifier.size(Sizes.s06),
    )
    Spacer(modifier = Modifier.height(Sizes.s02))
    WalletTexts.BodyLarge(
        text = stringResource(R.string.tk_walletPairing_devicePairingQRCode_fetchError_body),
        color = WalletTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    Buttons.FilledPrimary(
        text = stringResource(R.string.tk_walletPairing_devicePairingQRCode_fetchError_button),
        onClick = onRefresh,
    )
    Spacer(modifier = Modifier.height(Sizes.s02))
}

private typealias PreviewParams = Pair<QrBoxUiState, WalletPairingQrCodeUiState>
private class EIdWalletPairingQrCodePreviewParams : PreviewParameterProvider<PreviewParams> {
    override val values: Sequence<PreviewParams> = sequenceOf(
        QrBoxUiState.Success(
            qrBitmap = "This is my QR code data".generateQRBitmap()
        ) to WalletPairingQrCodeUiState.Polling,
        QrBoxUiState.Failure to WalletPairingQrCodeUiState.LoadingInvitationError,
        QrBoxUiState.Loading to WalletPairingQrCodeUiState.LoadingInvitation,
        QrBoxUiState.Loading to WalletPairingQrCodeUiState.NetworkError,
        QrBoxUiState.Loading to WalletPairingQrCodeUiState.PairingFailure,
    )
}

@WalletAllScreenPreview
@Composable
private fun EIdWalletPairingQrCodeScreenPreview(
    @PreviewParameter(EIdWalletPairingQrCodePreviewParams::class) params: PreviewParams,
) {
    WalletTheme {
        EIdWalletPairingQrCodeScreenContent(
            qrBoxState = params.first,
            screenState = params.second,
            onClose = {},
            onRefresh = {},
        )
    }
}
