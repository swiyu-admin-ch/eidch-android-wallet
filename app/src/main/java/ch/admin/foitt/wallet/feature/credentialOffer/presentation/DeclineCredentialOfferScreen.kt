package ch.admin.foitt.wallet.feature.credentialOffer.presentation

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
import ch.admin.foitt.wallet.platform.credential.presentation.CredentialActionFeedbackCard
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceState
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus
import ch.admin.foitt.wallet.theme.WalletTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeclineCredentialOfferScreen(
    viewModel: DeclineCredentialOfferViewModel,
) {
    val badgeBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val badgeBottomSheet = viewModel.badgeBottomSheet.collectAsStateWithLifecycle().value
    if (badgeBottomSheet != null) {
        BadgeBottomSheet(
            sheetState = badgeBottomSheetState,
            badgeBottomSheetUiState = badgeBottomSheet,
            onDismiss = viewModel::onDismissBottomSheet
        )
    }

    DeclineCredentialOfferScreenContent(
        isLoading = viewModel.isLoading.collectAsStateWithLifecycle().value,
        issuer = viewModel.uiState.collectAsStateWithLifecycle().value.issuer,
        onBadge = viewModel::onBadge,
        onCancel = viewModel::onCancel,
        onDecline = viewModel::onDecline,
    )
}

@Composable
private fun DeclineCredentialOfferScreenContent(
    isLoading: Boolean,
    issuer: ActorUiState,
    onBadge: (BadgeType) -> Unit,
    onCancel: () -> Unit,
    onDecline: () -> Unit,
) {
    CredentialActionFeedbackCard(
        isLoading = isLoading,
        issuer = issuer,
        contentTextFirstParagraphText = R.string.tk_receive_declineOffer_primary,
        contentTextSecondParagraphText = R.string.tk_receive_declineOffer_secondary,
        iconAlwaysVisible = false,
        contentIcon = R.drawable.wallet_ic_circular_questionmark,
        primaryButtonText = R.string.tk_receive_declineOffer_primaryButton,
        secondaryButtonText = R.string.tk_global_cancel,
        onPrimaryButton = onDecline,
        onSecondaryButton = onCancel,
        onBadge = onBadge,
    )
}

@WalletAllScreenPreview
@Composable
private fun DeclineCredentialOfferScreenContentPreview() {
    WalletTheme {
        DeclineCredentialOfferScreenContent(
            isLoading = false,
            issuer = ActorUiState(
                name = "Test Issuer",
                painter = painterResource(id = R.drawable.wallet_ic_scan_person),
                trustStatus = TrustStatus.TRUSTED,
                vcSchemaTrustStatus = VcSchemaTrustStatus.TRUSTED,
                actorType = ActorType.ISSUER,
                nonComplianceState = NonComplianceState.REPORTED,
                nonComplianceReason = "report reason",
            ),
            onBadge = {},
            onCancel = {},
            onDecline = {},
        )
    }
}
