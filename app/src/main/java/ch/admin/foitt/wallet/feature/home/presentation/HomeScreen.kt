package ch.admin.foitt.wallet.feature.home.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshDefaults
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.home.presentation.composables.EIdRequestCard
import ch.admin.foitt.wallet.feature.home.presentation.model.HomeContainerState
import ch.admin.foitt.wallet.feature.home.presentation.model.HomeScreenState
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.ToastAnimated
import ch.admin.foitt.wallet.platform.composables.presentation.WindowWidthClass
import ch.admin.foitt.wallet.platform.composables.presentation.layout.LazyColumn
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.composables.presentation.nonFocusableAccessibilityAnchor
import ch.admin.foitt.wallet.platform.composables.presentation.windowWidthClass
import ch.admin.foitt.wallet.platform.credential.presentation.CredentialListRow
import ch.admin.foitt.wallet.platform.credential.presentation.mock.CredentialMocks
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableProgressionState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.SIdRequestDisplayData
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.SIdRequestDisplayStatus
import ch.admin.foitt.wallet.platform.preview.AllCompactScreensPreview
import ch.admin.foitt.wallet.platform.preview.AllLargeScreensPreview
import ch.admin.foitt.wallet.platform.preview.ComposableWrapper
import ch.admin.foitt.wallet.platform.utils.TestTags
import ch.admin.foitt.wallet.platform.utils.TraversalIndex
import ch.admin.foitt.wallet.platform.utils.setIsTraversalGroup
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
) {
    val uiMode = LocalConfiguration.current.uiMode
    LaunchedEffect(uiMode) {
        viewModel.screenContentState.refreshData()
    }

    HomeScreenContent(
        screenState = viewModel.screenContentState.stateFlow.collectAsStateWithLifecycle().value,
        containerState = viewModel.homeContainerState.collectAsStateWithLifecycle().value,
        isRefreshing = viewModel.isRefreshing.collectAsStateWithLifecycle().value,
        eventMessage = viewModel.eventMessage.collectAsStateWithLifecycle().value,
        onMenu = viewModel::onMenu,
        onStartOnlineIdentification = viewModel::onStartOnlineIdentification,
        onCloseEId = viewModel::onCloseEId,
        onCloseToast = viewModel::onCloseToast,
        onRefresh = viewModel::onRefresh,
        onRefreshSIds = viewModel::onRefreshSIdStatuses,
        onObtainConsent = viewModel::onObtainConsent,
        onLearnMore = viewModel::onLearnMore
    )
}

@Composable
private fun HomeScreenContent(
    screenState: HomeScreenState,
    containerState: HomeContainerState,
    isRefreshing: Boolean,
    onMenu: (Boolean) -> Unit,
    onStartOnlineIdentification: (caseId: String) -> Unit,
    onCloseEId: (caseId: String) -> Unit,
    onCloseToast: () -> Unit,
    onRefresh: () -> Unit,
    onRefreshSIds: () -> Unit,
    onObtainConsent: (caseId: String) -> Unit,
    onLearnMore: () -> Unit,
    @StringRes eventMessage: Int?,
    windowWidthClass: WindowWidthClass = currentWindowAdaptiveInfo().windowWidthClass()
) = WalletLayouts.HomeContainer(
    windowWidthClass = windowWidthClass,
    containerState = containerState,
    onMenu = onMenu,
) { stickyBottomHeightDp ->

    when (screenState) {
        is HomeScreenState.Initial -> {
        }

        is HomeScreenState.CredentialList -> Credentials(
            credentialsState = screenState.credentials,
            isRefreshing = isRefreshing,
            ongoingEIdRequests = screenState.eIdRequests,
            onStartOnlineIdentification = onStartOnlineIdentification,
            onCloseEId = onCloseEId,
            contentBottomPadding = stickyBottomHeightDp,
            onCredentialClick = screenState.onCredentialClick,
            onRefresh = onRefresh,
            messageToast = eventMessage,
            onRefreshSIds = onRefreshSIds,
            onCloseToast = onCloseToast,
            onObtainConsent = onObtainConsent,
            onLearnMore = onLearnMore
        )

        is HomeScreenState.NoCredential -> WalletEmptyWithEIdRequestsContent(
            contentBottomPadding = stickyBottomHeightDp,
            isRefreshing = isRefreshing,
            ongoingEIdRequests = screenState.eIdRequests,
            onStartOnlineIdentification = onStartOnlineIdentification,
            onCloseEId = onCloseEId,
            showEIdRequestButton = containerState.showEIdRequestButton,
            showBetaIdRequestButton = containerState.showBetaIdRequestButton,
            onRequestEId = containerState.onGetEId,
            onRequestBetaId = containerState.onGetBetaId,
            onRefresh = onRefresh,
            onRefreshSIds = onRefreshSIds,
            onObtainConsent = onObtainConsent,
            onLearnMore = onLearnMore
        )

        is HomeScreenState.WalletEmpty -> WalletEmptyContent(
            contentBottomPadding = stickyBottomHeightDp,
            showEIdRequestButton = containerState.showEIdRequestButton,
            showBetaIdRequestButton = containerState.showBetaIdRequestButton,
            onRequestEId = containerState.onGetEId,
            onRequestBetaId = containerState.onGetBetaId,
        )

        HomeScreenState.UnexpectedError -> {
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WalletEmptyWithEIdRequestsContent(
    contentBottomPadding: Dp,
    isRefreshing: Boolean,
    ongoingEIdRequests: List<SIdRequestDisplayData>,
    onStartOnlineIdentification: (caseId: String) -> Unit,
    onCloseEId: (caseId: String) -> Unit,
    showEIdRequestButton: Boolean,
    showBetaIdRequestButton: Boolean,
    onRequestEId: () -> Unit,
    onRequestBetaId: () -> Unit,
    onRefresh: () -> Unit,
    onRefreshSIds: () -> Unit,
    onObtainConsent: (caseId: String) -> Unit,
    onLearnMore: () -> Unit,
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        refreshingOffset = pullToRefreshTopPadding(),
        onRefresh = onRefresh,
    )
    LazyColumn(
        state = rememberLazyListState(),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .padding(bottom = contentBottomPadding)
            .pullRefresh(
                state = pullRefreshState,
            )
            .setIsTraversalGroup(index = TraversalIndex.HIGH1),
    ) {
        item {
            Spacer(modifier = Modifier.height(Sizes.s04))
        }

        items(ongoingEIdRequests) { eIdRequest: SIdRequestDisplayData ->
            Box(modifier = Modifier.padding(horizontal = Sizes.s03)) {
                EIdRequestCard(
                    eIdRequest = eIdRequest,
                    onStartOnlineIdentification = { onStartOnlineIdentification(eIdRequest.caseId) },
                    onRefresh = onRefreshSIds,
                    onObtainConsent = { onObtainConsent(eIdRequest.caseId) },
                    onLearnMore = { onLearnMore() },
                    onCloseClick = { onCloseEId(eIdRequest.caseId) }
                )
            }
            Spacer(modifier = Modifier.height(Sizes.s02))
        }

        item {
            Column(
                modifier = Modifier.padding(vertical = Sizes.s04),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(80.dp))
                WalletEmptyContent(
                    showEIdRequestButton = showEIdRequestButton,
                    showBetaIdRequestButton = showBetaIdRequestButton,
                    onRequestEId = onRequestEId,
                    onRequestBetaId = onRequestBetaId,
                )
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
        )
    }
}

@Composable
fun BoxScope.WalletEmptyContent(
    contentBottomPadding: Dp,
    showEIdRequestButton: Boolean,
    showBetaIdRequestButton: Boolean,
    onRequestEId: () -> Unit,
    onRequestBetaId: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .align(Alignment.Center)
            .wrapContentHeight()
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .padding(bottom = contentBottomPadding)
            .padding(start = Sizes.s06, end = Sizes.s06, top = Sizes.s04, bottom = Sizes.s06)
            .widthIn(max = Sizes.maxTextWidth)
            .setIsTraversalGroup(index = TraversalIndex.HIGH1),
    ) {
        WalletEmptyContent(
            showEIdRequestButton = showEIdRequestButton,
            showBetaIdRequestButton = showBetaIdRequestButton,
            onRequestEId = onRequestEId,
            onRequestBetaId = onRequestBetaId,
        )
    }
}

@Composable
fun WalletEmptyContent(
    showEIdRequestButton: Boolean,
    showBetaIdRequestButton: Boolean,
    onRequestEId: () -> Unit,
    onRequestBetaId: () -> Unit,
) {
    NoCredentialIcon()
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleLarge(
        text = stringResource(id = R.string.tk_getBetaId_firstUse_title),
        textAlign = TextAlign.Center,
        modifier = Modifier
            .nonFocusableAccessibilityAnchor()
    )
    Spacer(modifier = Modifier.height(Sizes.s01))
    WalletTexts.Body(
        text = stringResource(id = R.string.tk_getBetaId_firstUse_body),
        textAlign = TextAlign.Center,
    )
    if (showBetaIdRequestButton || showEIdRequestButton) {
        Spacer(modifier = Modifier.height(Sizes.s06))
        Column(
            verticalArrangement = Arrangement.spacedBy(Sizes.s04),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (showEIdRequestButton) {
                Buttons.FilledTertiary(
                    text = stringResource(R.string.tk_global_getEid_greenButton),
                    onClick = onRequestEId,
                    startIcon = painterResource(id = R.drawable.wallet_ic_next_button)
                )
            }
            if (showBetaIdRequestButton) {
                Buttons.FilledTertiary(
                    text = stringResource(R.string.tk_global_getbetaid_primarybutton),
                    onClick = onRequestBetaId,
                    startIcon = painterResource(id = R.drawable.wallet_ic_next_button)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun Credentials(
    credentialsState: List<CredentialCardState>,
    isRefreshing: Boolean,
    @StringRes messageToast: Int?,
    onCloseToast: () -> Unit,
    contentBottomPadding: Dp,
    ongoingEIdRequests: List<SIdRequestDisplayData>,
    onStartOnlineIdentification: (caseId: String) -> Unit,
    onCloseEId: (id: String) -> Unit,
    onCredentialClick: (id: Long, progressState: VerifiableProgressionState) -> Unit,
    onRefresh: () -> Unit,
    onRefreshSIds: () -> Unit,
    onObtainConsent: (caseId: String) -> Unit,
    onLearnMore: () -> Unit,
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        refreshingOffset = pullToRefreshTopPadding(),
        onRefresh = onRefresh,
    )
    WalletLayouts.LazyColumn(
        useBottomInsets = false,
        modifier = Modifier
            .setIsTraversalGroup()
            .fillMaxHeight()
            .pullRefresh(state = pullRefreshState)
            .testTag(TestTags.CREDENTIAL_LIST.name),
        contentPadding = PaddingValues(
            top = Sizes.s06,
            bottom = contentBottomPadding + Sizes.s06
        )
    ) {
        if (ongoingEIdRequests.isNotEmpty()) {
            items(ongoingEIdRequests) { eIdRequest: SIdRequestDisplayData ->
                Box(modifier = Modifier.padding(horizontal = Sizes.s03)) {
                    EIdRequestCard(
                        eIdRequest = eIdRequest,
                        onStartOnlineIdentification = { onStartOnlineIdentification(eIdRequest.caseId) },
                        onRefresh = onRefreshSIds,
                        onObtainConsent = { onObtainConsent(eIdRequest.caseId) },
                        onLearnMore = { onLearnMore() },
                        onCloseClick = { onCloseEId(eIdRequest.caseId) }
                    )
                }
                Spacer(modifier = Modifier.height(Sizes.s02))
            }

            item {
                HorizontalDivider()
            }
        }

        items(credentialsState) { credentialState ->
            CredentialListRow(
                showDivider = true,
                credentialState = credentialState,
                onClick = {
                    onCredentialClick(
                        credentialState.credentialId,
                        credentialState.progressionState,
                    )
                },
            )
        }
    }
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
        )
    }

    ToastAnimated(
        isVisible = messageToast != null,
        isSnackBarDesign = true,
        messageToast = messageToast,
        onCloseToast = onCloseToast,
        iconEnd = R.drawable.wallet_ic_cross,
        contentBottomPadding = contentBottomPadding + Sizes.s06
    )
}

@Composable
private fun NoCredentialIcon() = Box(
    contentAlignment = Alignment.Center,
) {
    Image(
        painter = painterResource(id = R.drawable.wallet_ic_nocredential_bg),
        contentDescription = null,
        modifier = Modifier
            .width(Sizes.noCredentialThumbnailWidth)
            .testTag(TestTags.NO_CREDENTIAL_ICON.name)
    )
    Image(
        painter = painterResource(id = R.drawable.wallet_ic_nocredential_line),
        contentDescription = null,
        modifier = Modifier.width(Sizes.s20)
    )
}

private class HomePreviewParams : PreviewParameterProvider<ComposableWrapper<HomeScreenState>> {
    override val values: Sequence<ComposableWrapper<HomeScreenState>> = sequenceOf(
        ComposableWrapper {
            HomeScreenState.CredentialList(
                eIdRequests = emptyList(),
                credentials = CredentialMocks.cardStates.toList().map { it.value() },
                onCredentialClick = { _, _ -> },
            )
        },
        ComposableWrapper {
            HomeScreenState.CredentialList(
                eIdRequests = listOf(
                    SIdRequestDisplayData(
                        status = SIdRequestDisplayStatus.QUEUEING,
                        firstName = "Seraina",
                        lastName = "Muster",
                        caseId = "1",
                    ),
                    SIdRequestDisplayData(
                        status = SIdRequestDisplayStatus.AV_READY,
                        firstName = "Seraina",
                        lastName = "Muster",
                        caseId = "2",
                    )
                ),
                credentials = CredentialMocks.cardStates.toList().map { it.value() },
                onCredentialClick = { _, _ -> },
            )
        },
        ComposableWrapper {
            HomeScreenState.NoCredential(
                eIdRequests = emptyList(),
            )
        },
        ComposableWrapper {
            HomeScreenState.NoCredential(
                eIdRequests = listOf(
                    SIdRequestDisplayData(
                        status = SIdRequestDisplayStatus.QUEUEING,
                        firstName = "Seraina",
                        lastName = "Muster",
                        caseId = "1",

                    ),
                    SIdRequestDisplayData(
                        status = SIdRequestDisplayStatus.AV_READY,
                        firstName = "Seraina",
                        lastName = "Muster",
                        caseId = "2"
                    )
                ),
            )
        },
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun pullToRefreshTopPadding() = PullRefreshDefaults.RefreshingOffset +
    WindowInsets.safeDrawing.only(WindowInsetsSides.Top).asPaddingValues().calculateTopPadding()

@AllCompactScreensPreview
@Composable
private fun HomeScreenCompactPreview(
    @PreviewParameter(HomePreviewParams::class) state: ComposableWrapper<HomeScreenState>,
) {
    WalletTheme {
        HomeScreenContent(
            screenState = state.value(),
            containerState = HomeContainerState.EMPTY,
            windowWidthClass = WindowWidthClass.COMPACT,
            isRefreshing = true,
            eventMessage = R.string.tk_home_notification_credential_declined,
            onMenu = {},
            onStartOnlineIdentification = {},
            onCloseEId = {},
            onRefresh = {},
            onRefreshSIds = {},
            onObtainConsent = {},
            onCloseToast = {},
            onLearnMore = {}
        )
    }
}

@AllLargeScreensPreview
@Composable
private fun HomeScreenLargePreview(
    @PreviewParameter(HomePreviewParams::class) state: ComposableWrapper<HomeScreenState>,
) {
    WalletTheme {
        HomeScreenContent(
            screenState = state.value(),
            containerState = HomeContainerState.EMPTY,
            windowWidthClass = WindowWidthClass.EXPANDED,
            isRefreshing = false,
            eventMessage = R.string.tk_home_notification_credential_declined,
            onMenu = {},
            onStartOnlineIdentification = {},
            onCloseEId = {},
            onRefresh = {},
            onRefreshSIds = {},
            onObtainConsent = {},
            onCloseToast = {},
            onLearnMore = {}
        )
    }
}
