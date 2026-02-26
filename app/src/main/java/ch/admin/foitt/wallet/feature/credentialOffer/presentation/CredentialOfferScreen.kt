package ch.admin.foitt.wallet.feature.credentialOffer.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.credentialOffer.presentation.model.CredentialOfferUiState
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.InvitationHeader
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.badges.domain.model.BadgeType
import ch.admin.foitt.wallet.platform.badges.presentation.BadgeBottomSheet
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.ConfirmationBottomSheet
import ch.admin.foitt.wallet.platform.composables.HiddenScrollToButton
import ch.admin.foitt.wallet.platform.composables.HiddenScrollToTopButton
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.presentation.HeightReportingLayout
import ch.admin.foitt.wallet.platform.composables.presentation.WindowWidthClass
import ch.admin.foitt.wallet.platform.composables.presentation.horizontalSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.layout.LazyColumn
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.composables.presentation.verticalSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.windowWidthClass
import ch.admin.foitt.wallet.platform.credential.presentation.MediumCredentialCard
import ch.admin.foitt.wallet.platform.credential.presentation.credentialClaimItems
import ch.admin.foitt.wallet.platform.credential.presentation.mock.CredentialMocks
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceState
import ch.admin.foitt.wallet.platform.preview.AllCompactScreensPreview
import ch.admin.foitt.wallet.platform.preview.AllLargeScreensPreview
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus
import ch.admin.foitt.wallet.platform.utils.TestTags
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialOfferScreen(
    viewModel: CredentialOfferViewModel,
) {
    BackHandler {
        viewModel.onDeclineClicked()
    }

    val uiMode = LocalConfiguration.current.uiMode
    LaunchedEffect(uiMode) {
        viewModel.credentialOfferUiState.refreshData()
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
            title = R.string.tk_receive_credentialOffer_confirmIssuance_primary,
            body = R.string.tk_receive_credentialOffer_confirmIssuance_secondary,
            acceptButtonText = R.string.tk_receive_credentialOffer_confirmIssuance_button_primary,
            declineButtonText = R.string.tk_receive_credentialOffer_confirmIssuance_button_secondary,
            onAccept = viewModel::acceptCredential,
            onDecline = viewModel::onDeclineBottomSheet,
            onDismiss = viewModel::onDismissConfirmationBottomSheet,
        )
    }

    CredentialOfferScreenContent(
        isLoading = viewModel.isLoading.collectAsStateWithLifecycle().value,
        credentialOfferUiState = viewModel.credentialOfferUiState.stateFlow.collectAsStateWithLifecycle().value,
        onBadge = viewModel::onBadge,
        onAccept = viewModel::onAcceptClicked,
        onDecline = viewModel::onDeclineClicked,
        onWrongData = viewModel::onReportWrongDataClicked,
    )
}

@Composable
private fun CredentialOfferScreenContent(
    isLoading: Boolean,
    credentialOfferUiState: CredentialOfferUiState,
    onBadge: (BadgeType) -> Unit,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onWrongData: () -> Unit,
) = Box(
    modifier = Modifier
        .fillMaxSize()
        .background(WalletTheme.colorScheme.surfaceContainerLow)
) {
    when (currentWindowAdaptiveInfo().windowWidthClass()) {
        WindowWidthClass.COMPACT -> CompactContent(
            credentialOffer = credentialOfferUiState,
            onBadge = onBadge,
            onAccept = onAccept,
            onDecline = onDecline,
            onWrongData = onWrongData,
        )

        else -> LargeContent(
            credentialOffer = credentialOfferUiState,
            onBadge = onBadge,
            onAccept = onAccept,
            onDecline = onDecline,
            onWrongData = onWrongData,
        )
    }
    LoadingOverlay(showOverlay = isLoading)
}

@Composable
private fun CompactContent(
    credentialOffer: CredentialOfferUiState,
    onBadge: (BadgeType) -> Unit,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onWrongData: () -> Unit,
) {
    val lazyListState = rememberLazyListState()
    HiddenScrollToButton(
        text = stringResource(id = R.string.tk_receive_credentialOffer_hiddenGoToDetails),
        lazyListState = lazyListState,
        index = 3,
    )
    HiddenScrollToTopButton(
        text = stringResource(id = R.string.tk_global_hiddenGoToTop),
        lazyListState = lazyListState,
    )
    WalletLayouts.LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        state = lazyListState,
        useTopInsets = false,
        contentPadding = PaddingValues(bottom = Sizes.s06),
    ) {
        item {
            InvitationHeader(
                actorUiState = credentialOffer.issuer,
                onBadge = onBadge,
            )
        }
        item {
            WalletTexts.BodyLarge(
                modifier = Modifier.padding(horizontal = Sizes.s06, vertical = Sizes.s03),
                text = stringResource(R.string.tk_receive_credentialOffer_headerSection_secondary),
            )
        }

        item {
            CredentialBoxCompact(
                credential = credentialOffer.credential,
                onAccept = onAccept,
                onDecline = onDecline,
            )
            Spacer(modifier = Modifier.height(Sizes.s04))
        }

        credentialClaimItems(
            claimItems = credentialOffer.claims,
            onWrongData = onWrongData,
        )

        item {
            CredentialOfferButtons(
                modifier = Modifier
                    .padding(horizontal = Sizes.s04)
                    .padding(top = Sizes.s10),
                onAccept = onAccept,
                onDecline = onDecline,
            )
        }
    }
}

@Composable
private fun CredentialBoxCompact(
    credential: CredentialCardState,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = WalletTheme.colorScheme.background,
                shape = RoundedCornerShape(Sizes.credentialCardCorner),
            )
            .padding(vertical = Sizes.s10, horizontal = Sizes.s04),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MediumCredentialCard(
            modifier = Modifier
                .padding(horizontal = Sizes.s10)
                .testTag(TestTags.OFFER_CREDENTIAL.name),
            credentialCardState = credential,
        )
        Spacer(modifier = Modifier.height(Sizes.s06))
        CredentialOfferButtons(
            onAccept = onAccept,
            onDecline = onDecline,
        )
    }
}

@Composable
private fun LargeContent(
    credentialOffer: CredentialOfferUiState,
    onBadge: (BadgeType) -> Unit,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onWrongData: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.horizontalSafeDrawing()) {
            Spacer(modifier = Modifier.width(Sizes.s04))
            CredentialBoxLarge(
                modifier = Modifier.width(this@BoxWithConstraints.maxWidth * 0.33f),
                credential = credentialOffer.credential,
            )
            Spacer(modifier = Modifier.width(Sizes.s04))
            DetailsWithHeader(
                credentialOffer = credentialOffer,
                onBadge = onBadge,
                onAccept = onAccept,
                onDecline = onDecline,
                onWrongData = onWrongData,
            )
        }
    }
}

@Composable
private fun CredentialBoxLarge(
    modifier: Modifier,
    credential: CredentialCardState,
) {
    Box(
        modifier = modifier
            .verticalSafeDrawing()
            .padding(vertical = Sizes.s02),
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = WalletTheme.colorScheme.background,
                    shape = RoundedCornerShape(Sizes.credentialCardCorner),
                )
                .padding(Sizes.s04),
        ) {
            MediumCredentialCard(
                credentialCardState = credential,
                isScrollingEnabled = true,
            )
        }
    }
}

@Composable
private fun DetailsWithHeader(
    credentialOffer: CredentialOfferUiState,
    onBadge: (BadgeType) -> Unit,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onWrongData: () -> Unit,
) {
    val lazyListState = rememberLazyListState()
    val stickyBottomHeight = remember { mutableStateOf(0.dp) }
    Box(modifier = Modifier.fillMaxSize()) {
        WalletLayouts.LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            state = lazyListState,
            useTopInsets = false,
            contentPadding = PaddingValues(bottom = Sizes.s02 + stickyBottomHeight.value),
        ) {
            item {
                InvitationHeader(
                    actorUiState = credentialOffer.issuer,
                    onBadge = onBadge,
                )
            }

            item {
                WalletTexts.BodyLarge(
                    text = stringResource(R.string.tk_receive_credentialOffer_headerSection_secondary),
                    modifier = Modifier.padding(horizontal = Sizes.s04)
                )
                Spacer(modifier = Modifier.height(Sizes.s04))
            }

            credentialClaimItems(
                claimItems = credentialOffer.claims,
                onWrongData = onWrongData,
            )

            item {
                Spacer(modifier = Modifier.height(Sizes.s06))
            }
        }
        StickyButtons(
            modifier = Modifier.align(Alignment.BottomCenter),
            stickyBottomHeight,
            onDecline,
            onAccept
        )
        HiddenScrollToTopButton(
            text = stringResource(id = R.string.tk_global_hiddenGoToTop),
            lazyListState = lazyListState,
        )
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun StickyButtons(
    modifier: Modifier = Modifier,
    stickyBottomHeight: MutableState<Dp>,
    onDecline: () -> Unit,
    onAccept: () -> Unit
) {
    HeightReportingLayout(
        modifier = modifier,
        onContentHeightMeasured = { height -> stickyBottomHeight.value = height },
    ) {
        FlowRow(
            modifier = Modifier
                .background(WalletTheme.colorScheme.surface.copy(alpha = 0.85f))
                .fillMaxWidth()
                .padding(top = Sizes.s04, end = Sizes.s04, bottom = Sizes.s02)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                .focusGroup(),
            horizontalArrangement = Arrangement.spacedBy(Sizes.s02, Alignment.End),
            verticalArrangement = Arrangement.spacedBy(Sizes.s02, Alignment.Top),
            maxItemsInEachRow = 2,
        ) {
            Buttons.FilledPrimary(
                modifier = Modifier
                    .weight(1f)
                    .testTag(TestTags.STICKY_DECLINE_BUTTON.name),
                text = stringResource(id = R.string.tk_receive_credentialOffer_button_decline),
                startIcon = painterResource(id = R.drawable.wallet_ic_cross),
                onClick = onDecline,
            )
            Buttons.FilledTertiary(
                modifier = Modifier
                    .weight(1f)
                    .testTag(TestTags.STICKY_ACCEPT_BUTTON.name),
                text = stringResource(id = R.string.tk_receive_credentialOffer_button_accept),
                startIcon = painterResource(id = R.drawable.wallet_ic_checkmark),
                onClick = onAccept,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CredentialOfferButtons(
    modifier: Modifier = Modifier,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Sizes.s02, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(Sizes.s02, Alignment.Top),
        maxItemsInEachRow = 2,
    ) {
        Buttons.FilledPrimary(
            modifier = Modifier
                .weight(1f)
                .testTag(TestTags.DECLINE_BUTTON.name),
            text = stringResource(id = R.string.tk_receive_credentialOffer_button_decline),
            startIcon = painterResource(id = R.drawable.wallet_ic_cross),
            onClick = onDecline,
        )
        Buttons.FilledTertiary(
            modifier = Modifier
                .weight(1f)
                .testTag(TestTags.ACCEPT_BUTTON.name),
            text = stringResource(id = R.string.tk_receive_credentialOffer_button_accept),
            startIcon = painterResource(id = R.drawable.wallet_ic_checkmark),
            onClick = onAccept,
        )
    }
}

@AllCompactScreensPreview
@Composable
private fun CredentialOfferScreenPreview() {
    WalletTheme {
        CredentialOfferScreenContent(
            isLoading = false,
            credentialOfferUiState = CredentialOfferUiState(
                issuer = ActorUiState(
                    name = "Test Issuer",
                    painter = painterResource(id = R.drawable.ic_swiss_cross_small),
                    trustStatus = TrustStatus.TRUSTED,
                    vcSchemaTrustStatus = VcSchemaTrustStatus.TRUSTED,
                    actorType = ActorType.ISSUER,
                    nonComplianceState = NonComplianceState.REPORTED,
                    nonComplianceReason = "report reason",
                ),
                credential = CredentialMocks.cardState01,
                claims = CredentialMocks.clusterList,
            ),
            onBadge = {},
            onAccept = {},
            onDecline = {},
            onWrongData = {},
        )
    }
}

@AllLargeScreensPreview
@Composable
private fun CredentialOfferLargeContentPreview() {
    WalletTheme {
        LargeContent(
            credentialOffer = CredentialOfferUiState(
                issuer = ActorUiState(
                    name = "Test Issuer",
                    painter = painterResource(id = R.drawable.ic_swiss_cross_small),
                    trustStatus = TrustStatus.TRUSTED,
                    vcSchemaTrustStatus = VcSchemaTrustStatus.TRUSTED,
                    actorType = ActorType.ISSUER,
                    nonComplianceState = NonComplianceState.REPORTED,
                    nonComplianceReason = "report reason",
                ),
                credential = CredentialMocks.cardState01,
                claims = CredentialMocks.clusterList,
            ),
            onBadge = {},
            onAccept = {},
            onDecline = {},
            onWrongData = {},
        )
    }
}
