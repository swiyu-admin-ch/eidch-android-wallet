package ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.ScreenMainImage
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.DocumentScannerErrorType
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
internal fun EIdDocumentScannerErrorScreen(
    viewModel: EIdDocumentScannerErrorViewModel,
) {
    BackHandler {
        viewModel.onClose()
    }

    when (viewModel.errorType) {
        DocumentScannerErrorType.GENERIC -> {
            EIdDocumentScannerErrorScreenContent(
                onClose = viewModel::onClose,
                title = R.string.tk_global_error_unexpected_title,
                body = R.string.tk_global_error_unexpected_message,
                buttonText = R.string.tk_global_close_alt
            )
        }
        DocumentScannerErrorType.UNEQUAL_DOCUMENTS -> {
            EIdDocumentScannerErrorScreenContent(
                onClose = viewModel::onClose,
                title = R.string.tk_eidRequest_documentScan_wrongDocument_primary,
                body = R.string.tk_eidRequest_documentScan_wrongDocument_secondary,
                buttonText = R.string.tk_eidRequest_documentScan_wrongDocument_button
            )
        }
    }
}

@Composable
private fun EIdDocumentScannerErrorScreenContent(
    @StringRes title: Int,
    @StringRes body: Int,
    @StringRes buttonText: Int,
    onClose: () -> Unit,
) = WalletLayouts.ScrollableColumnWithPicture(
    stickyStartContent = {
        ScreenMainImage(
            iconRes = R.drawable.wallet_ic_cross_circle_colored,
            backgroundColor = WalletTheme.colorScheme.surfaceContainerLow
        )
    },
    stickyBottomBackgroundColor = Color.Transparent,
    stickyBottomContent = {
        Buttons.FilledPrimary(
            text = stringResource(buttonText),
            onClick = onClose,
            modifier = Modifier.fillMaxWidth()
        )
    },
) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleScreen(
        text = stringResource(title),
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.BodyLarge(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(body),
    )
}

@WalletAllScreenPreview
@Composable
private fun DocumentScannerErrorScreenPreview() {
    WalletTheme {
        EIdDocumentScannerErrorScreenContent(
            onClose = {},
            title = R.string.tk_eidRequest_documentScan_wrongDocument_primary,
            body = R.string.tk_eidRequest_documentScan_wrongDocument_secondary,
            buttonText = R.string.tk_eidRequest_documentScan_wrongDocument_button
        )
    }
}
