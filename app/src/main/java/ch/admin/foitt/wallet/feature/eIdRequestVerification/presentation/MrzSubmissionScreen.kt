package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation

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
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.model.MrzSubmissionUiState
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
internal fun MrzSubmissionScreen(
    viewModel: MrzSubmissionViewModel,
) {
    MrzSubmissionScreenContent(
        applySubmissionState = viewModel.mrzState.collectAsStateWithLifecycle().value,
    )
}

@Composable
private fun MrzSubmissionScreenContent(
    applySubmissionState: MrzSubmissionUiState,
) = when (applySubmissionState) {
    MrzSubmissionUiState.Loading -> LoadingContent()
    MrzSubmissionUiState.Valid -> LoadingContent()
    is MrzSubmissionUiState.Unexpected -> UnexpectedErrorContent(
        onClose = applySubmissionState.onClose,
        onRetry = applySubmissionState.onRetry
    )

    is MrzSubmissionUiState.NetworkError -> UnexpectedErrorContent(
        onClose = applySubmissionState.onClose,
        onRetry = applySubmissionState.onRetry
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
        text = stringResource(id = R.string.tk_eidRequest_mrz_loading_primary),
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.BodyLarge(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(id = R.string.tk_eidRequest_mrz_loading_secondary),
    )
}

@Composable
private fun UnexpectedErrorContent(
    onClose: () -> Unit,
    onRetry: () -> Unit,
) = WalletLayouts.ScrollableColumnWithPicture(
    stickyStartContent = {
        ScreenMainImage(
            iconRes = R.drawable.wallet_ic_sadface_colored,
            backgroundColor = WalletTheme.colorScheme.surfaceContainerLow
        )
    },
    stickyBottomBackgroundColor = Color.Transparent,
    stickyBottomContent = {
        Buttons.FilledPrimary(
            text = stringResource(R.string.tk_eidRequest_attestation_unexpectedError_button_retry),
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
        )
        Buttons.FilledSecondary(
            text = stringResource(R.string.tk_eidRequest_attestation_unexpectedError_button_close),
            onClick = onClose,
            modifier = Modifier
                .fillMaxWidth()
        )
    },
) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleScreen(
        text = stringResource(id = R.string.tk_eidRequest_attestation_unexpectedError_primary),
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.BodyLarge(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(id = R.string.tk_eidRequest_attestation_unexpectedError_secondary),
    )
}

private class MrzSubmissionPreviewParams : PreviewParameterProvider<MrzSubmissionUiState> {
    override val values: Sequence<MrzSubmissionUiState> = sequenceOf(
        MrzSubmissionUiState.Valid,
        MrzSubmissionUiState.Unexpected({}, {}),
        MrzSubmissionUiState.NetworkError({}, {}),
    )
}

@WalletAllScreenPreview
@Composable
private fun MrzSubmissionScreenPreview(
    @PreviewParameter(MrzSubmissionPreviewParams::class) state: MrzSubmissionUiState,
) {
    WalletTheme {
        MrzSubmissionScreenContent(
            applySubmissionState = state,
        )
    }
}
