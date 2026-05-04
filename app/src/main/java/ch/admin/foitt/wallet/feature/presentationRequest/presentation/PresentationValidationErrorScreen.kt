package ch.admin.foitt.wallet.feature.presentationRequest.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.badges.domain.model.BadgeType
import ch.admin.foitt.wallet.platform.badges.presentation.BadgeBottomSheet
import ch.admin.foitt.wallet.platform.credential.presentation.CredentialActionFeedbackCardError
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.ActorComplianceState
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus
import ch.admin.foitt.wallet.theme.WalletTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresentationValidationErrorScreen(viewModel: PresentationValidationErrorViewModel) {
    val badgeBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val badgeBottomSheet = viewModel.badgeBottomSheet.collectAsStateWithLifecycle().value
    if (badgeBottomSheet != null) {
        BadgeBottomSheet(
            sheetState = badgeBottomSheetState,
            badgeBottomSheetUiState = badgeBottomSheet,
            onDismiss = viewModel::onDismissBottomSheet
        )
    }

    PresentationValidationErrorContent(
        verifierUiState = viewModel.verifierUiState.collectAsStateWithLifecycle().value,
        onClose = viewModel::onClose,
        onBadge = viewModel::onBadge
    )
}

@Composable
private fun PresentationValidationErrorContent(
    verifierUiState: ActorUiState,
    onClose: () -> Unit,
    onBadge: (BadgeType) -> Unit,
) {
    CredentialActionFeedbackCardError(
        issuer = verifierUiState,
        contentTextFirstParagraphText = R.string.tk_present_verifierError_title,
        contentTextSecondParagraphText = R.string.tk_present_verifierError_subtitle,
        contentIcon = R.drawable.wallet_ic_error_general,
        iconAlwaysVisible = true,
        primaryButtonText = R.string.tk_global_close,
        onPrimaryButton = onClose,
        onBadge = onBadge,
    )
}

@Composable
@WalletAllScreenPreview
private fun PresentationValidationErrorPreview() {
    WalletTheme {
        PresentationValidationErrorContent(
            verifierUiState = ActorUiState(
                name = "My Verifier Name",
                painter = painterResource(R.drawable.wallet_ic_error_general),
                trustStatus = TrustStatus.TRUSTED,
                vcSchemaTrustStatus = VcSchemaTrustStatus.TRUSTED,
                actorType = ActorType.VERIFIER,
                actorComplianceState = ActorComplianceState.REPORTED,
                nonComplianceReason = "report reason",
            ),
            onClose = {},
            onBadge = {},
        )
    }
}
