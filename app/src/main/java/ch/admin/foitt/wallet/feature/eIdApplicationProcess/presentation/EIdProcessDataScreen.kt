package ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.model.ProcessDataUiState
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.LoadingIndicator
import ch.admin.foitt.wallet.platform.composables.presentation.ScreenMainImage
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
internal fun EIdProcessDataScreen(
    viewModel: EIdProcessDataViewModel,
) {
    EIdProcessDataScreenContent(
        processDataState = viewModel.state.collectAsStateWithLifecycle().value,
    )
}

@Composable
private fun EIdProcessDataScreenContent(
    processDataState: ProcessDataUiState,
) = when (processDataState) {
    ProcessDataUiState.Loading,
    ProcessDataUiState.Valid -> LoadingContent()

    is ProcessDataUiState.Declined -> DeclinedErrorContent(
        onClose = processDataState.onClose,
        onHelp = processDataState.onHelp,
    )

    is ProcessDataUiState.GenericError -> GenericErrorContent(
        onClose = processDataState.onClose,
        onRetry = processDataState.onRetry,
        onHelp = processDataState.onHelp,
    )
}

@Composable
private fun LoadingContent() = WalletLayouts.ScrollableColumnWithPicture(
    stickyStartContent = {
        LoadingIndicator()
    },
    stickyBottomBackgroundColor = Color.Transparent,
    stickyBottomContent = null,
) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleScreen(
        text = stringResource(id = R.string.tk_eidRequest_dataProcess_loading_primary),
    )
}

@Composable
private fun DeclinedErrorContent(
    onClose: () -> Unit,
    onHelp: () -> Unit,
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
            text = stringResource(R.string.tk_eidRequest_dataProcess_declined_primaryButton),
            onClick = onClose,
            modifier = Modifier
                .fillMaxWidth()
        )
    },
) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleScreen(
        text = stringResource(id = R.string.tk_eidRequest_dataProcess_declined_primary),
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.BodyLarge(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(id = R.string.tk_eidRequest_dataProcess_declined_secondary),
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    Buttons.TextLink(
        text = stringResource(id = R.string.tk_eidRequest_dataProcess_link_textButton),
        onClick = onHelp,
        endIcon = painterResource(id = R.drawable.wallet_ic_chevron),
    )
}

@Composable
private fun GenericErrorContent(
    onClose: () -> Unit,
    onRetry: () -> Unit,
    onHelp: () -> Unit,
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
            text = stringResource(R.string.tk_eidRequest_dataProcess_error_primaryButton),
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
        )
        Buttons.TonalSecondary(
            text = stringResource(R.string.tk_eidRequest_dataProcess_error_secondaryButton),
            onClick = onClose,
            modifier = Modifier
                .fillMaxWidth()
        )
    },
) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleScreen(
        text = stringResource(id = R.string.tk_eidRequest_dataProcess_error_primary),
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.BodyLarge(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(id = R.string.tk_eidRequest_dataProcess_error_secondary),
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    Buttons.TextLink(
        text = stringResource(id = R.string.tk_eidRequest_dataProcess_link_textButton),
        onClick = onHelp,
        endIcon = painterResource(id = R.drawable.wallet_ic_chevron),
    )
}

private class EIdProcessDataPreviewParams : PreviewParameterProvider<ProcessDataUiState> {
    override val values: Sequence<ProcessDataUiState> = sequenceOf(
        ProcessDataUiState.Valid,
        ProcessDataUiState.Declined({}, {}),
        ProcessDataUiState.GenericError({}, {}, {}),
    )
}

@WalletAllScreenPreview
@Composable
private fun EIdProcessDataScreenPreview(
    @PreviewParameter(EIdProcessDataPreviewParams::class) state: ProcessDataUiState,
) {
    WalletTheme {
        EIdProcessDataScreenContent(
            processDataState = state,
        )
    }
}
