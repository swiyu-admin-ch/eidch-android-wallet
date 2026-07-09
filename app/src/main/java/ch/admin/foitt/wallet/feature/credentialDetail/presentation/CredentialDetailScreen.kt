package ch.admin.foitt.wallet.feature.credentialDetail.presentation

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.credentialDetail.presentation.composables.CredentialDeleteBottomSheet
import ch.admin.foitt.wallet.feature.credentialDetail.presentation.composables.MenuBottomSheet
import ch.admin.foitt.wallet.feature.credentialDetail.presentation.composables.VisibleBottomSheet
import ch.admin.foitt.wallet.feature.credentialDetail.presentation.model.CredentialDetailUiState
import ch.admin.foitt.wallet.feature.onboarding.presentation.composables.CollectFocusEvents
import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityType
import ch.admin.foitt.wallet.platform.activityList.presentation.composables.activityListItem
import ch.admin.foitt.wallet.platform.activityList.presentation.composables.disabledHistoryActivityListItem
import ch.admin.foitt.wallet.platform.activityList.presentation.composables.emptyHistoryActivityListItem
import ch.admin.foitt.wallet.platform.activityList.presentation.composables.entireHistoryButton
import ch.admin.foitt.wallet.platform.activityList.presentation.composables.goToHistorySettingsButton
import ch.admin.foitt.wallet.platform.activityList.presentation.model.ActivityUiState
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.presentation.WindowWidthClass
import ch.admin.foitt.wallet.platform.composables.presentation.horizontalSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.layout.LazyColumn
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.composables.presentation.scrollToTopOnStuckFocus
import ch.admin.foitt.wallet.platform.composables.presentation.windowWidthClass
import ch.admin.foitt.wallet.platform.credential.presentation.CredentialCardCreditFormat
import ch.admin.foitt.wallet.platform.credential.presentation.credentialElements
import ch.admin.foitt.wallet.platform.credential.presentation.mock.CredentialMocks
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.ActorComplianceState
import ch.admin.foitt.wallet.platform.preview.AllCompactScreensPreview
import ch.admin.foitt.wallet.platform.preview.AllLargeScreensPreview
import ch.admin.foitt.wallet.platform.scaffold.presentation.LocalScaffoldPaddings
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialDetailScreen(
    viewModel: CredentialDetailViewModel,
) {
    val scope = rememberCoroutineScope()
    val credentialHistoryFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    CollectFocusEvents(viewModel.focusEvents) {
        focusManager.clearFocus()
        credentialHistoryFocusRequester.requestFocus()
    }
    val menuSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val deleteSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val uiMode = LocalConfiguration.current.uiMode
    LaunchedEffect(uiMode) {
        viewModel.credentialDetailUiState.refreshData()
    }

    val visibleBottomSheet = viewModel.visibleBottomSheet.collectAsStateWithLifecycle().value
    if (visibleBottomSheet == VisibleBottomSheet.MENU) {
        MenuBottomSheet(
            sheetState = menuSheetState,
            onDismiss = {
                scope.hideModalSheet(menuSheetState, onHidden = viewModel::onBottomSheetDismiss)
            },
            onDelete = {
                scope.hideModalSheet(menuSheetState, onHidden = viewModel::onDelete)
            },
            onUpdate = {
                scope.hideModalSheet(menuSheetState, onHidden = viewModel::onUpdate)
            }
        )
    }
    if (visibleBottomSheet == VisibleBottomSheet.DELETE) {
        CredentialDeleteBottomSheet(
            sheetState = deleteSheetState,
            onDismiss = {
                scope.hideModalSheet(deleteSheetState, onHidden = viewModel::onBottomSheetDismiss)
            },
            onDeleteCredential = {
                scope.hideModalSheet(deleteSheetState, onHidden = viewModel::onDeleteCredential)
            },
        )
    }

    CredentialDetailScreenContent(
        isLoading = viewModel.isLoading.collectAsStateWithLifecycle().value,
        credentialDetail = viewModel.credentialDetailUiState.stateFlow.collectAsStateWithLifecycle().value,
        credentialHistoryFocusRequester = credentialHistoryFocusRequester,
        onEntireHistory = viewModel::onEntireHistory,
        onActivitySettings = viewModel::onActivitySettings,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
private fun CoroutineScope.hideModalSheet(
    sheetState: SheetState,
    onHidden: () -> Unit,
) {
    launch { sheetState.hide() }.invokeOnCompletion {
        if (!sheetState.isVisible) {
            onHidden()
        }
    }
}

@Composable
private fun CredentialDetailScreenContent(
    isLoading: Boolean,
    credentialDetail: CredentialDetailUiState,
    credentialHistoryFocusRequester: FocusRequester,
    windowWidthClass: WindowWidthClass = currentWindowAdaptiveInfo().windowWidthClass(),
    onEntireHistory: () -> Unit,
    onActivitySettings: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = WalletTheme.colorScheme.surfaceContainerLow)
    ) {
        when (windowWidthClass) {
            WindowWidthClass.COMPACT -> CredentialDetailCompact(
                credentialDetail = credentialDetail,
                credentialHistoryFocusRequester = credentialHistoryFocusRequester,
                onEntireHistory = onEntireHistory,
                onActivitySettings = onActivitySettings,
            )

            else -> CredentialDetailLarge(
                credentialDetail = credentialDetail,
                credentialHistoryFocusRequester = credentialHistoryFocusRequester,
                onEntireHistory = onEntireHistory,
                onActivitySettings = onActivitySettings,
            )
        }
        LoadingOverlay(showOverlay = isLoading)
    }
}

@Composable
private fun CredentialDetailCompact(
    credentialDetail: CredentialDetailUiState,
    credentialHistoryFocusRequester: FocusRequester,
    onEntireHistory: () -> Unit,
    onActivitySettings: () -> Unit,
) {
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    WalletLayouts.LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .scrollToTopOnStuckFocus(lazyListState = scrollState, coroutineScope = coroutineScope),
        state = scrollState,
        contentPadding = PaddingValues(vertical = Sizes.s04),
        useTopInsets = false,
    ) {
        item {
            WalletLayouts.TopInsetSpacer(
                shouldScrollUnderTopBar = true,
                scaffoldPaddings = LocalScaffoldPaddings.current,
            )
        }

        item {
            CredentialCardCreditFormat(
                credentialCardState = credentialDetail.credential,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Sizes.s04),
            )
        }

        item {
            Spacer(Modifier.height(Sizes.s04))
        }

        latestActivities(
            areActivitiesEnabled = credentialDetail.areActivitiesEnabled,
            activities = credentialDetail.activities,
            credentialHistoryFocusRequester = credentialHistoryFocusRequester,
            onEntireHistory = onEntireHistory,
            onActivitySettings = onActivitySettings,
        )

        item {
            Spacer(modifier = Modifier.height(Sizes.s06))
        }

        credentialElements(
            elements = credentialDetail.clusterItems,
            showIssuer = true,
            issuer = credentialDetail.issuer.name,
            issuerIcon = credentialDetail.issuer.painter,
        )
    }
}

@Composable
private fun CredentialDetailLarge(
    credentialDetail: CredentialDetailUiState,
    credentialHistoryFocusRequester: FocusRequester,
    onEntireHistory: () -> Unit,
    onActivitySettings: () -> Unit,
) {
    Column {
        WalletLayouts.TopInsetSpacer(
            shouldScrollUnderTopBar = true,
            scaffoldPaddings = LocalScaffoldPaddings.current,
        )

        Row(
            modifier = Modifier.horizontalSafeDrawing()
        ) {
            CredentialCardCreditFormat(
                credentialCardState = credentialDetail.credential,
                modifier = Modifier
                    .semantics { isTraversalGroup = true }
                    .weight(1f)
                    .padding(Sizes.s04),
            )

            val lazyListState = rememberLazyListState()
            val coroutineScope = rememberCoroutineScope()
            WalletLayouts.LazyColumn(
                modifier = Modifier
                    .semantics { isTraversalGroup = true }
                    .weight(1f)
                    .scrollToTopOnStuckFocus(lazyListState = lazyListState, coroutineScope = coroutineScope),
                state = lazyListState,
                contentPadding = PaddingValues(Sizes.s04),
                useTopInsets = false,
            ) {
                latestActivities(
                    areActivitiesEnabled = credentialDetail.areActivitiesEnabled,
                    activities = credentialDetail.activities,
                    credentialHistoryFocusRequester = credentialHistoryFocusRequester,
                    onEntireHistory = onEntireHistory,
                    onActivitySettings = onActivitySettings,
                )

                item {
                    Spacer(modifier = Modifier.height(Sizes.s06))
                }

                credentialElements(
                    elements = credentialDetail.clusterItems,
                    showIssuer = true,
                    issuer = credentialDetail.issuer.name,
                    issuerIcon = credentialDetail.issuer.painter,
                )
            }
        }
    }
}

private fun LazyListScope.latestActivities(
    areActivitiesEnabled: Boolean,
    activities: List<ActivityUiState>,
    credentialHistoryFocusRequester: FocusRequester,
    onEntireHistory: () -> Unit,
    onActivitySettings: () -> Unit,
) {
    val contentPadding = PaddingValues(horizontal = Sizes.s04)
    item {
        WalletTexts.ClusterHeadline(
            text = stringResource(R.string.tk_activity_latestActivities_title),
            depth = 0
        )
        Spacer(modifier = Modifier.height(Sizes.s02))
    }

    if (areActivitiesEnabled) {
        enabledActivityList(
            contentPadding = contentPadding,
            credentialHistoryFocusRequester = credentialHistoryFocusRequester,
            activities = activities,
            onEntireHistory = onEntireHistory,
        )
    } else {
        disabledActivityList(
            contentPadding = contentPadding,
            onActivitySettings = onActivitySettings,
        )
    }
}

private fun LazyListScope.enabledActivityList(
    contentPadding: PaddingValues,
    credentialHistoryFocusRequester: FocusRequester,
    activities: List<ActivityUiState>,
    onEntireHistory: () -> Unit,
) {
    if (activities.isEmpty()) {
        emptyHistoryActivityListItem(paddingValues = contentPadding)
    } else {
        activities.forEachIndexed { index, activity ->
            this.activityListItem(
                activityType = activity.activityType,
                activityId = activity.id,
                activityActorName = activity.localizedActorName,
                activityDate = activity.date,
                isFirstItem = index == activities.indices.first,
                isLastItem = false, // it can never be the last item, because of the entire history button
                paddingValues = contentPadding,
            )
        }

        entireHistoryButton(
            paddingValues = contentPadding,
            credentialHistoryFocusRequester = credentialHistoryFocusRequester,
            onClick = onEntireHistory,
        )
    }
}

private fun LazyListScope.disabledActivityList(
    contentPadding: PaddingValues,
    onActivitySettings: () -> Unit,
) {
    disabledHistoryActivityListItem(paddingValues = contentPadding)
    goToHistorySettingsButton(
        paddingValues = contentPadding,
        onClick = onActivitySettings,
    )
}

@AllCompactScreensPreview
@Composable
private fun CredentialDetailScreenCompactPreview() {
    WalletTheme {
        CredentialDetailScreenPreview(windowWidthClass = WindowWidthClass.COMPACT)
    }
}

@AllLargeScreensPreview
@Composable
private fun CredentialDetailScreenLargePreview() {
    WalletTheme {
        CredentialDetailScreenPreview(windowWidthClass = WindowWidthClass.EXPANDED)
    }
}

@SuppressLint("RememberInComposition")
@Composable
private fun CredentialDetailScreenPreview(windowWidthClass: WindowWidthClass) {
    CredentialDetailScreenContent(
        isLoading = false,
        credentialDetail = CredentialDetailUiState(
            credential = CredentialMocks.cardState01,
            clusterItems = CredentialMocks.clusterList,
            issuer = ActorUiState(
                name = "Issuer",
                painter = painterResource(id = R.drawable.ic_swiss_cross_small),
                trustStatus = TrustStatus.TRUSTED,
                vcSchemaTrustStatus = VcSchemaTrustStatus.TRUSTED,
                actorType = ActorType.ISSUER,
                actorComplianceState = ActorComplianceState.REPORTED,
                nonComplianceReason = "report reason",
            ),
            areActivitiesEnabled = true,
            activities = listOf(
                ActivityUiState(
                    id = 1,
                    activityType = ActivityType.ISSUANCE,
                    date = "01.01.2025 | 12:34",
                    localizedActorName = "actor name",
                )
            ),
        ),
        credentialHistoryFocusRequester = FocusRequester(),
        windowWidthClass = windowWidthClass,
        onEntireHistory = {},
        onActivitySettings = {}
    )
}
