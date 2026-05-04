package ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation

import androidx.activity.compose.BackHandler
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
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.model.AttestationUiState
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
internal fun EIdAttestationScreen(
    viewModel: EIdAttestationViewModel,
) {
    BackHandler {
        viewModel.onClose()
    }

    EIdAttestationScreenContent(
        attestationState = viewModel.attestationState.collectAsStateWithLifecycle().value,
    )
}

@Composable
private fun EIdAttestationScreenContent(
    attestationState: AttestationUiState,
) = when (attestationState) {
    AttestationUiState.Loading,
    AttestationUiState.Valid -> LoadingContent()
    is AttestationUiState.InvalidClientAttestation -> InvalidClientContent(
        onClose = attestationState.onClose,
        onHelp = attestationState.onHelp,
        onPlaystore = attestationState.onPlaystore,
    )
    is AttestationUiState.InvalidKeyAttestation -> InvalidKeyContent(
        onClose = attestationState.onClose,
        onHelp = attestationState.onHelp,
    )
    is AttestationUiState.NetworkError -> NetworkErrorContent(
        titleText = R.string.tk_eidRequest_clientAttestation_service_error_title,
        bodyText = R.string.tk_eidRequest_clientAttestation_service_error_body,
        onClose = attestationState.onClose,
        onRetry = attestationState.onRetry,
    )
    is AttestationUiState.IntegrityError -> IntegrityErrorContent(
        onClose = attestationState.onClose,
    )
    is AttestationUiState.IntegrityNetworkError -> NetworkErrorContent(
        titleText = R.string.tk_eidRequest_clientAttestation_android_platform_timeout_title,
        bodyText = R.string.tk_eidRequest_clientAttestation_android_platform_timeout_body,
        onClose = attestationState.onClose,
        onRetry = attestationState.onRetry,
    )
    is AttestationUiState.Unexpected -> UnexpectedErrorContent(
        onClose = attestationState.onClose,
        onRetry = attestationState.onRetry,
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
        text = stringResource(id = R.string.tk_eidRequest_attestation_loading_primary),
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.BodyLarge(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(id = R.string.tk_eidRequest_attestation_loading_secondary),
    )
}

@Composable
private fun InvalidKeyContent(
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
            text = stringResource(R.string.tk_eidRequest_attestation_deviceNotSupported_button_close),
            onClick = onClose,
            modifier = Modifier.fillMaxWidth()
        )
    },
) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleScreen(
        text = stringResource(id = R.string.tk_eidRequest_clientAttestation_insufficientKeyStorage_title),
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.BodyLarge(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(id = R.string.tk_eidRequest_clientAttestation_insufficientKeyStorage_body),
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    Buttons.TextLink(
        text = stringResource(id = R.string.tk_eidRequest_attestation_deviceNotSupported_link_text),
        onClick = onHelp,
        endIcon = painterResource(id = R.drawable.wallet_ic_chevron),
    )
}

@Composable
private fun IntegrityErrorContent(
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
            text = stringResource(R.string.tk_eidRequest_attestation_deviceNotSupported_button_close),
            onClick = onClose,
            modifier = Modifier.fillMaxWidth()
        )
    },
) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleScreen(
        text = stringResource(id = R.string.tk_eidRequest_clientAttestation_android_platform_error_title),
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.BodyLarge(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(id = R.string.tk_eidRequest_clientAttestation_android_platform_error_body),
    )
}

private class EIdAttestationPreviewParams : PreviewParameterProvider<AttestationUiState> {
    override val values: Sequence<AttestationUiState> = sequenceOf(
        AttestationUiState.Loading,
        AttestationUiState.Valid,
        AttestationUiState.InvalidClientAttestation({}, {}, {}),
        AttestationUiState.InvalidKeyAttestation({}, {}),
        AttestationUiState.NetworkError({}, {}),
        AttestationUiState.IntegrityError {},
    )
}

@WalletAllScreenPreview
@Composable
private fun EIdAttestationScreenPreview(
    @PreviewParameter(EIdAttestationPreviewParams::class) state: AttestationUiState,
) {
    WalletTheme {
        EIdAttestationScreenContent(
            attestationState = state,
        )
    }
}
