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
import ch.admin.foitt.wallet.platform.credential.presentation.CredentialActionFeedbackCardSuccess
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceState
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus
import ch.admin.foitt.wallet.theme.WalletTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresentationSuccessScreen(viewModel: PresentationSuccessViewModel) {
    val badgeBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val badgeBottomSheet = viewModel.badgeBottomSheet.collectAsStateWithLifecycle().value
    if (badgeBottomSheet != null) {
        BadgeBottomSheet(
            sheetState = badgeBottomSheetState,
            badgeBottomSheetUiState = badgeBottomSheet,
            onDismiss = viewModel::onDismissBottomSheet
        )
    }

    PresentationSuccessContent(
        verifierUiState = viewModel.verifierUiState.collectAsStateWithLifecycle().value,
        fields = viewModel.sentFields,
        onClose = viewModel::onClose,
        onBadge = viewModel::onBadge
    )
}

@Composable
private fun PresentationSuccessContent(
    verifierUiState: ActorUiState,
    fields: List<String>,
    onClose: () -> Unit,
    onBadge: (BadgeType) -> Unit,
) {
    CredentialActionFeedbackCardSuccess(
        issuer = verifierUiState,
        contentTextFirstParagraphText = R.string.tk_present_result_success_primary,
        iconAlwaysVisible = true,
        contentIcon = R.drawable.wallet_ic_check_circle_complete_thin,
        primaryButtonText = R.string.tk_global_close,
        onPrimaryButton = onClose,
        content = { SubmittedDataBox(fields = fields) },
        onBadge = onBadge,
    )
}

@Composable
@WalletAllScreenPreview
private fun PresentationSuccessPreview() {
    WalletTheme {
        PresentationSuccessContent(
            verifierUiState = ActorUiState(
                name = "My Verfifier Name",
                painter = painterResource(id = R.drawable.ic_swiss_cross_small),
                trustStatus = TrustStatus.TRUSTED,
                vcSchemaTrustStatus = VcSchemaTrustStatus.TRUSTED,
                actorType = ActorType.VERIFIER,
                nonComplianceState = NonComplianceState.REPORTED,
                nonComplianceReason = "report reason",
            ),
            fields = listOf("name", "firstname", "country", "age", "employment"),
            onClose = {},
            onBadge = {},
        )
    }
}
