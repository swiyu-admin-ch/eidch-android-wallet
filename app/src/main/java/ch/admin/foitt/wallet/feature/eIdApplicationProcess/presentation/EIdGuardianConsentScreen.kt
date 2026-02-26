package ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.model.QrBoxUiState
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.utils.OnResumeEventHandler
import ch.admin.foitt.wallet.platform.utils.generateQRBitmap
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
internal fun EIdGuardianConsentScreen(
    viewModel: EIdGuardianConsentViewModel,
) {
    EIdGuardianConsentScreenContent(
        isRequestLoading = viewModel.isRequestLoading.collectAsStateWithLifecycle().value,
        isRequestStatusLoading = viewModel.isRequestStatusLoading.collectAsStateWithLifecycle().value,
        qrBoxState = viewModel.qrBoxState.collectAsStateWithLifecycle().value,
        onRefresh = viewModel::onRefreshRequest,
        onShareRequest = viewModel::onShareRequest,
        onContinue = viewModel::onContinue,
    )

    OnResumeEventHandler {
        viewModel.onRefreshRequest()
    }
}

@Composable
private fun EIdGuardianConsentScreenContent(
    isRequestLoading: Boolean,
    isRequestStatusLoading: Boolean,
    qrBoxState: QrBoxUiState,
    onRefresh: () -> Unit,
    onShareRequest: () -> Unit,
    onContinue: () -> Unit,
) {
    WalletLayouts.ScrollableColumnWithPicture(
        stickyStartContent = {
            QrBox(
                state = qrBoxState,
                onRefresh = onRefresh,
            )
        },
        stickyBottomBackgroundColor = Color.Transparent,
        stickyBottomContent = {
            Buttons.TonalSecondary(
                text = stringResource(R.string.tk_getEid_guardianConsent_button_share),
                onClick = onShareRequest,
                enabled = !isRequestStatusLoading && !isRequestLoading && qrBoxState is QrBoxUiState.Success,
            )
            Buttons.FilledPrimary(
                text = stringResource(R.string.tk_getEid_guardianConsent_button_finish),
                onClick = onContinue,
                enabled = !isRequestStatusLoading && !isRequestLoading,
                isActive = isRequestStatusLoading,
                activeText = stringResource(R.string.tk_getEid_guardianConsent_button_finish),
            )
        },
    ) {
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.TitleScreen(
            text = stringResource(id = R.string.tk_getEid_guardianConsent_primary),
        )
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.BodyLarge(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.tk_getEid_guardianConsent_secondary),
        )
    }
}

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
        contentDescription = stringResource(R.string.tk_getEid_guardianConsent_qr_alt),
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
        text = stringResource(R.string.tk_getEid_guardianConsent_qr_error),
        color = WalletTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    Buttons.FilledPrimary(
        text = stringResource(R.string.tk_getEid_guardianConsent_qr_button_retry),
        onClick = onRefresh,
    )
    Spacer(modifier = Modifier.height(Sizes.s02))
}

private class EIdGuardianConsentPreviewParams : PreviewParameterProvider<QrBoxUiState> {
    override val values: Sequence<QrBoxUiState> = sequenceOf(
        QrBoxUiState.Failure,
        QrBoxUiState.Loading,
        QrBoxUiState.Success(
            qrBitmap = "This is my QR code data".generateQRBitmap()
        ),
    )
}

@WalletAllScreenPreview
@Composable
private fun EIdGuardianConsentScreenPreview(
    @PreviewParameter(EIdGuardianConsentPreviewParams::class) state: QrBoxUiState,
) {
    WalletTheme {
        EIdGuardianConsentScreenContent(
            qrBoxState = state,
            isRequestLoading = false,
            isRequestStatusLoading = false,
            onShareRequest = {},
            onContinue = {},
            onRefresh = {},
        )
    }
}
