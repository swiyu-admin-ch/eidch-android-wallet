package ch.admin.foitt.wallet.feature.presentationRequest.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
import ch.admin.foitt.wallet.platform.composables.AdaptiveBottomButtonBar
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.ConfirmationBottomSheet
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.presentation.HeightReportingLayout
import ch.admin.foitt.wallet.platform.composables.presentation.WalletLinearProgressIndicator
import ch.admin.foitt.wallet.platform.composables.presentation.WindowWidthClass
import ch.admin.foitt.wallet.platform.composables.presentation.layout.LazyColumn
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.composables.presentation.windowWidthClass
import ch.admin.foitt.wallet.platform.credential.presentation.CredentialCardSmall
import ch.admin.foitt.wallet.platform.credential.presentation.credentialElements
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
            title = viewModel.confirmationBottomSheetTitle,
            body = viewModel.confirmationBottomSheetBody,
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
        submissionProgress = viewModel.proximitySubmissionProgress.collectAsStateWithLifecycle().value,
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
    submissionProgress: Double?,
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
                submissionProgress = submissionProgress,
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

        if (submissionProgress == null) {
            LoadingOverlay(showOverlay = isLoading)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IsSubmittingContent(
    verifierUiState: ActorUiState,
    credentialCardState: CredentialCardState,
    submissionProgress: Double?,
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

                if (submissionProgress == null) {
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
                } else {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier
                            .padding(Sizes.s06)
                            .widthIn(max = 240.dp)
                            .constrainAs(loading) {
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                                bottom.linkTo(parent.bottom)
                            }
                    ) {
                        WalletLinearProgressIndicator(
                            progress = { submissionProgress.toFloat() },
                            modifier = Modifier
                                .height(Sizes.s02),
                        )
                        WalletTexts.BodyMedium(
                            "${(submissionProgress * 100).toInt()}%",
                            modifier = Modifier
                                .padding(top = Sizes.s01)
                        )
                    }
                }
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
) {
    var buttonsHeight by remember { mutableStateOf(0.dp) }

    val windowWidthClass = currentWindowAdaptiveInfo().windowWidthClass()
    val maxWidth = remember(windowWidthClass) {
        if (windowWidthClass == WindowWidthClass.COMPACT) 1.0f else 0.8f
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        WalletLayouts.LazyColumn(
            modifier = modifier,
            state = rememberLazyListState(),
            useTopInsets = false,
            useBottomInsets = false,
            contentPadding = PaddingValues(
                bottom = Sizes.s04 + buttonsHeight
            )
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

            credentialElements(
                elements = presentationRequestUiState.requestedClaims,
                onWrongData = onWrongData,
            )
        }

        Buttons(
            onDecline = onDecline,
            onAccept = onSubmit,
            onHeightMeasured = { buttonsHeight = it },
            modifier = Modifier
                .fillMaxWidth(maxWidth)
                .align(Alignment.BottomCenter)
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
    onHeightMeasured: (Dp) -> Unit,
    modifier: Modifier = Modifier
) {
    HeightReportingLayout(
        modifier = modifier,
        onContentHeightMeasured = onHeightMeasured
    ) {
        AdaptiveBottomButtonBar(
            buttons = listOf(
                {
                    Buttons.FilledTertiary(
                        modifier = Modifier.testTag(TestTags.ACCEPT_BUTTON.name),
                        text = stringResource(id = R.string.tk_present_review_button_accept),
                        startIcon = painterResource(id = R.drawable.wallet_ic_checkmark),
                        onClick = onAccept,
                    )
                },
                {
                    Buttons.FilledPrimary(
                        modifier = Modifier.testTag(TestTags.DECLINE_BUTTON.name),
                        text = stringResource(id = R.string.tk_present_review_button_decline),
                        startIcon = painterResource(id = R.drawable.wallet_ic_cross),
                        onClick = onDecline,
                    )
                }
            ),
            stacked = false
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
            submissionProgress = 0.5,
            showDelayReason = false,
            onWrongData = {},
            onSubmit = {},
            onDecline = {},
            onBadge = {},
        )
    }
}
