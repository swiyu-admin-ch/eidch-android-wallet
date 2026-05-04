package ch.admin.foitt.wallet.feature.presentationRequest.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.model.PresentationRequestUiState
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.InvitationHeader
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.badges.domain.model.BadgeType
import ch.admin.foitt.wallet.platform.badges.presentation.BadgeBottomSheet
import ch.admin.foitt.wallet.platform.badges.presentation.model.ClaimBadgeUiState
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.ConfirmationBottomSheet
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.presentation.WindowWidthClass
import ch.admin.foitt.wallet.platform.composables.presentation.layout.LazyColumn
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.composables.presentation.windowWidthClass
import ch.admin.foitt.wallet.platform.credential.presentation.CredentialCardSmall
import ch.admin.foitt.wallet.platform.credential.presentation.credentialClaimItems
import ch.admin.foitt.wallet.platform.credential.presentation.credentialInfoWithClaimBadgesWidget
import ch.admin.foitt.wallet.platform.credential.presentation.mock.CredentialMocks
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.ActorComplianceState
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus
import ch.admin.foitt.wallet.platform.utils.TestTags
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresentationRequestScreen(viewModel: PresentationRequestViewModel) {
    BackHandler(onBack = viewModel::onDecline)

    val uiMode = LocalConfiguration.current.uiMode
    LaunchedEffect(uiMode) {
        viewModel.presentationRequestUiState.refreshData()
    }

    val badgeBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val badgeBottomSheet = viewModel.badgeBottomSheet.collectAsStateWithLifecycle().value
    if (badgeBottomSheet != null) {
        BadgeBottomSheet(
            sheetState = badgeBottomSheetState,
            badgeBottomSheetUiState = badgeBottomSheet,
            onDismiss = viewModel::onDismissBadgeBottomSheet
        )
    }

    val confirmationBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val showConfirmationBottomSheet = viewModel.showConfirmationBottomSheet.collectAsStateWithLifecycle().value
    if (showConfirmationBottomSheet) {
        ConfirmationBottomSheet(
            sheetState = confirmationBottomSheetState,
            title = R.string.tk_present_review_confirmPresentation_primary,
            body = R.string.tk_present_review_confirmPresentation_secondary,
            acceptButtonText = R.string.tk_present_review_confirmPresentation_button_primary,
            declineButtonText = R.string.tk_present_review_confirmPresentation_button_secondary,
            onAccept = viewModel::submit,
            onDecline = viewModel::onDecline,
            onDismiss = viewModel::onDismissConfirmationBottomSheet,
        )
    }

    val presentationRequestUiState = viewModel.presentationRequestUiState.stateFlow.collectAsStateWithLifecycle().value
    val verifierUiState = viewModel.verifierUiState.collectAsStateWithLifecycle().value

    PresentationRequestContent(
        verifierUiState = verifierUiState,
        presentationRequestUiState = presentationRequestUiState,
        isLoading = viewModel.isLoading.collectAsStateWithLifecycle().value,
        isSubmitting = viewModel.isSubmitting.collectAsStateWithLifecycle().value,
        showDelayReason = viewModel.showDelayReason.collectAsStateWithLifecycle().value,
        onWrongData = viewModel::onReportWrongData,
        onSubmit = viewModel::onAccept,
        onDecline = viewModel::onDecline,
        onBadge = viewModel::onBadge,
    )
}

@Composable
private fun PresentationRequestContent(
    verifierUiState: ActorUiState,
    presentationRequestUiState: PresentationRequestUiState,
    isLoading: Boolean,
    isSubmitting: Boolean,
    showDelayReason: Boolean,
    onWrongData: () -> Unit,
    onSubmit: () -> Unit,
    onDecline: () -> Unit,
    onBadge: (BadgeType) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = WalletTheme.colorScheme.surfaceContainerLow)
    ) {
        val modifier = when (currentWindowAdaptiveInfo().windowWidthClass()) {
            WindowWidthClass.COMPACT -> Modifier.fillMaxWidth()
            else ->
                Modifier
                    .fillMaxWidth(0.8f)
                    .align(Alignment.Center)
        }

        if (isSubmitting) {
            IsSubmittingContent(
                verifierUiState = verifierUiState,
                credentialCardState = presentationRequestUiState.credentialCardState,
                modifier = modifier,
                showDelayReason = showDelayReason,
                onBadge = onBadge,
            )
        } else {
            ContentList(
                verifierUiState = verifierUiState,
                presentationRequestUiState = presentationRequestUiState,
                modifier = modifier,
                onWrongData = onWrongData,
                onSubmit = onSubmit,
                onDecline = onDecline,
                onBadge = onBadge,
            )
        }

        LoadingOverlay(showOverlay = isLoading)
    }
}

@Composable
private fun IsSubmittingContent(
    verifierUiState: ActorUiState,
    credentialCardState: CredentialCardState,
    modifier: Modifier,
    showDelayReason: Boolean,
    onBadge: (BadgeType) -> Unit,
) {
    Column(modifier = modifier) {
        Header(
            verifierUiState = verifierUiState,
            onBadge = onBadge,
        )
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = Sizes.boxCornerSize,
                        topEnd = Sizes.boxCornerSize
                    )
                )
                .background(WalletTheme.colorScheme.surfaceContainerLow),
            contentAlignment = Alignment.Center
        ) {
            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                val (
                    credentialCard,
                    loading,
                ) = createRefs()

                CredentialCardSmall(
                    credentialState = credentialCardState,
                    modifier = Modifier.constrainAs(credentialCard) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                    },
                )

                LoadingIndicator(
                    showDelayReason = showDelayReason,
                    modifier = Modifier
                        .constrainAs(loading) {
                            top.linkTo(credentialCard.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            bottom.linkTo(parent.bottom)
                        }
                        .padding(top = Sizes.s06)
                )
            }
        }
    }
}

@Composable
private fun ContentList(
    verifierUiState: ActorUiState,
    presentationRequestUiState: PresentationRequestUiState,
    modifier: Modifier,
    onBadge: (BadgeType) -> Unit,
    onWrongData: () -> Unit,
    onSubmit: () -> Unit,
    onDecline: () -> Unit,
) = WalletLayouts.LazyColumn(
    modifier = modifier,
    state = rememberLazyListState(),
    useTopInsets = false,
    useBottomInsets = true,
) {
    item {
        Header(
            verifierUiState = verifierUiState,
            onBadge = onBadge,
        )
    }
    item { Spacer(modifier = Modifier.height(Sizes.s04)) }

    item {
        WalletTexts.HeadlineSmallEmphasized(
            text = stringResource(id = R.string.tk_present_review_credential_dataSection_primary),
            modifier = Modifier
                .padding(start = Sizes.s06, end = Sizes.s03, top = Sizes.s02)
                .semantics { heading() },
        )
    }
    item { Spacer(modifier = Modifier.height(Sizes.s04)) }

    credentialInfoWithClaimBadgesWidget(
        credentialCardState = presentationRequestUiState.credentialCardState,
        claimBadgesUiStates = presentationRequestUiState.claimBadgesUiStates,
        onBadge = onBadge
    )
    item { Spacer(modifier = Modifier.height(Sizes.s04)) }

    credentialClaimItems(
        claimItems = presentationRequestUiState.requestedClaims,
        onWrongData = onWrongData,
    )
    item { Spacer(modifier = Modifier.height(Sizes.s04)) }

    item {
        Buttons(
            onDecline = onDecline,
            onAccept = onSubmit,
        )
    }
}

@Composable
private fun Header(
    verifierUiState: ActorUiState,
    onBadge: (BadgeType) -> Unit,
) {
    InvitationHeader(
        actorUiState = verifierUiState,
        onBadge = onBadge,
    )
}

@Composable
private fun LoadingIndicator(
    modifier: Modifier = Modifier,
    showDelayReason: Boolean
) = Box(
    modifier = modifier,
    contentAlignment = Alignment.BottomCenter,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = WalletTheme.colorScheme.primary,
            modifier = Modifier
                .padding(bottom = Sizes.s02)
                .size(Sizes.s12),
            strokeWidth = Sizes.line02,
        )
        if (showDelayReason) {
            WalletTexts.Body(text = stringResource(R.string.tk_present_review_loading))
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun Buttons(
    onDecline: () -> Unit,
    onAccept: () -> Unit,
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(corner = CornerSize(Sizes.s16)))
            .background(WalletTheme.colorScheme.background)
            .padding(vertical = Sizes.s03, horizontal = Sizes.s04)
            .focusGroup(),
        horizontalArrangement = Arrangement.spacedBy(Sizes.s02, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(Sizes.s02, Alignment.Bottom),
        maxItemsInEachRow = 2,
    ) {
        Buttons.FilledPrimary(
            modifier = Modifier.testTag(TestTags.DECLINE_BUTTON.name),
            text = stringResource(id = R.string.tk_present_review_button_decline),
            startIcon = painterResource(id = R.drawable.wallet_ic_cross),
            onClick = onDecline,
        )
        Buttons.FilledTertiary(
            modifier = Modifier.testTag(TestTags.ACCEPT_BUTTON.name),
            text = stringResource(id = R.string.tk_present_review_button_accept),
            startIcon = painterResource(id = R.drawable.wallet_ic_checkmark),
            onClick = onAccept,
        )
    }
}

@WalletAllScreenPreview
@Composable
private fun PresentationRequestScreenPreview() {
    WalletTheme {
        PresentationRequestContent(
            verifierUiState = ActorUiState(
                name = "My Verifier Name",
                painter = painterResource(id = R.drawable.ic_swiss_cross_small),
                trustStatus = TrustStatus.TRUSTED,
                vcSchemaTrustStatus = VcSchemaTrustStatus.TRUSTED,
                actorType = ActorType.VERIFIER,
                actorComplianceState = ActorComplianceState.REPORTED,
                nonComplianceReason = "report reason",
            ),
            presentationRequestUiState = PresentationRequestUiState(
                credentialCardState = CredentialMocks.cardState01,
                requestedClaims = CredentialMocks.clusterList,
                claimBadgesUiStates = listOf(
                    ClaimBadgeUiState(
                        localizedLabel = "Sensitive Claim",
                        isSensitive = true
                    ),
                    ClaimBadgeUiState(
                        localizedLabel = "Claim 2",
                        isSensitive = false
                    ),
                    ClaimBadgeUiState(
                        localizedLabel = "Non-sensitive Claim",
                        isSensitive = false
                    ),
                    ClaimBadgeUiState(
                        localizedLabel = "Some Claim",
                        isSensitive = false
                    ),
                ),
                numberOfClaims = 5
            ),
            isLoading = false,
            isSubmitting = false,
            showDelayReason = false,
            onWrongData = {},
            onSubmit = {},
            onDecline = {},
            onBadge = {},
        )
    }
}
