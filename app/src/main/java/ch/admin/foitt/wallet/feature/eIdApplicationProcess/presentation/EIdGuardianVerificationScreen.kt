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
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.model.GuardianVerificationUiState
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.LoadingIndicator
import ch.admin.foitt.wallet.platform.composables.presentation.ScreenMainImage
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.utils.OnResumeEventHandler
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
internal fun EIdGuardianVerificationScreen(viewModel: EIdGuardianVerificationViewModel) {
    OnResumeEventHandler {
        viewModel.onResume()
    }

    EIdGuardianVerificationScreenContent(
        uiState = viewModel.uiState.collectAsStateWithLifecycle().value,
    )
}

@Composable
private fun EIdGuardianVerificationScreenContent(
    uiState: GuardianVerificationUiState,
) {
    when (uiState) {
        is GuardianVerificationUiState.Info -> InfoContent(
            isLoading = uiState.isLoading,
            onStart = uiState.onStart,
        )
        is GuardianVerificationUiState.Loading -> LoadingContent(
            onCancel = uiState.onCancel,
        )
        is GuardianVerificationUiState.NetworkError -> NetworkErrorContent(
            onRetry = uiState.onRetry,
            onClose = uiState.onClose,
        )
        is GuardianVerificationUiState.NoValidCredential -> NoValidCredentialContent(
            onRequest = uiState.onRequestEId,
            onCancel = uiState.onCancel,
            onHelp = uiState.onHelp,
        )
        is GuardianVerificationUiState.UnexpectedError -> UnexpectedErrorContent(
            onClose = uiState.onClose,
        )
    }
}

//region Screen state content
@Composable
private fun InfoContent(
    isLoading: Boolean,
    onStart: () -> Unit,
) = WalletLayouts.ScrollableColumnWithPicture(
    stickyStartContent = {
        ScreenMainImage(
            iconRes = R.drawable.wallet_ic_person_checkmark_colored,
        )
    },
    stickyBottomBackgroundColor = Color.Transparent,
    stickyBottomContent = {
        Buttons.FilledPrimary(
            text = stringResource(R.string.tk_eidRequest_guardianVerification_info_button_start),
            onClick = onStart,
            enabled = !isLoading,
            isActive = isLoading,
            modifier = Modifier.fillMaxWidth()
        )
    },
) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleLarge(
        text = stringResource(id = R.string.tk_eidRequest_guardianVerification_info_primary),
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.BodyLarge(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(id = R.string.tk_eidRequest_guardianVerification_info_secondary),
    )
}

@Composable
private fun NoValidCredentialContent(
    onRequest: () -> Unit,
    onCancel: () -> Unit,
    onHelp: () -> Unit,
) = WalletLayouts.ScrollableColumnWithPicture(
    stickyStartContent = {
        ScreenMainImage(
            iconRes = R.drawable.wallet_ic_cross_circle_colored,
        )
    },
    stickyBottomBackgroundColor = Color.Transparent,
    stickyBottomContent = {
        Buttons.FilledPrimary(
            text = stringResource(R.string.tk_eidRequest_guardianVerification_noCredential_button_requestEId),
            onClick = onRequest,
            modifier = Modifier.fillMaxWidth()
        )
        Buttons.TonalSecondary(
            text = stringResource(R.string.tk_eidRequest_guardianVerification_noCredential_button_cancel),
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        )
    },
) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleScreen(
        text = stringResource(id = R.string.tk_eidRequest_guardianVerification_noCredential_primary),
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.BodyLarge(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(id = R.string.tk_eidRequest_guardianVerification_noCredential_secondary),
    )
    Spacer(modifier = Modifier.height(Sizes.s04))
    Buttons.TextLink(
        text = stringResource(id = R.string.tk_eidRequest_guardianVerification_noCredential_link_text),
        onClick = onHelp,
        endIcon = painterResource(id = R.drawable.wallet_ic_chevron),
    )
}

@Composable
private fun NetworkErrorContent(
    onRetry: () -> Unit,
    onClose: () -> Unit,
) = WalletLayouts.ScrollableColumnWithPicture(
    stickyStartContent = {
        ScreenMainImage(
            iconRes = R.drawable.wallet_ic_cross_circle_colored,
        )
    },
    stickyBottomBackgroundColor = Color.Transparent,
    stickyBottomContent = {
        Buttons.FilledPrimary(
            text = stringResource(R.string.tk_eidRequest_guardianVerification_networkError_button_retry),
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth()
        )
        Buttons.TonalSecondary(
            text = stringResource(R.string.tk_eidRequest_guardianVerification_networkError_button_cancel),
            onClick = onClose,
            modifier = Modifier.fillMaxWidth()
        )
    },
) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleScreen(
        text = stringResource(id = R.string.tk_eidRequest_guardianVerification_networkError_primary),
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.BodyLarge(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(id = R.string.tk_eidRequest_guardianVerification_networkError_secondary),
    )
}

@Composable
private fun UnexpectedErrorContent(
    onClose: () -> Unit,
) = WalletLayouts.ScrollableColumnWithPicture(
    stickyStartContent = {
        ScreenMainImage(
            iconRes = R.drawable.wallet_ic_cross_circle_colored,
        )
    },
    stickyBottomBackgroundColor = Color.Transparent,
    stickyBottomContent = {
        Buttons.FilledPrimary(
            text = stringResource(R.string.tk_eidRequest_guardianVerification_unexpectedError_button_cancel),
            onClick = onClose,
            modifier = Modifier.fillMaxWidth()
        )
    },
) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleScreen(
        text = stringResource(id = R.string.tk_eidRequest_guardianVerification_unexpectedError_primary),
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.BodyLarge(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(id = R.string.tk_eidRequest_guardianVerification_unexpectedError_secondary),
    )
}

@Composable
private fun LoadingContent(
    onCancel: () -> Unit
) = WalletLayouts.ScrollableColumnWithPicture(
    stickyStartContent = {
        LoadingIndicator()
    },
    stickyBottomBackgroundColor = Color.Transparent,
    stickyBottomContent = {
        Buttons.FilledPrimary(
            text = stringResource(R.string.tk_eidRequest_guardianVerification_loading_button_cancel),
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        )
    },
) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleScreen(
        text = stringResource(id = R.string.tk_eidRequest_guardianVerification_loading_primary),
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.BodyLarge(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(id = R.string.tk_eidRequest_guardianVerification_loading_secondary),
    )
}
//endregion

//region Preview
private class EIdGuardianVerificationPreviewParams : PreviewParameterProvider<GuardianVerificationUiState> {
    override val values: Sequence<GuardianVerificationUiState> = sequenceOf(
        GuardianVerificationUiState.Info(false, {}),
        GuardianVerificationUiState.Loading({}),
        GuardianVerificationUiState.NoValidCredential({}, {}, {}),
        GuardianVerificationUiState.NetworkError({}, {}),
        GuardianVerificationUiState.UnexpectedError({}),
    )
}

@WalletAllScreenPreview
@Composable
private fun EIdGuardianVerificationScreenPreview(
    @PreviewParameter(EIdGuardianVerificationPreviewParams::class) uiState: GuardianVerificationUiState,
) {
    WalletTheme {
        EIdGuardianVerificationScreenContent(
            uiState = uiState,
        )
    }
}
//endregion
