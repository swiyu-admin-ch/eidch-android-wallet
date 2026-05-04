package ch.admin.foitt.wallet.feature.deferredDetail.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.credentialDetail.presentation.composables.CredentialDeleteBottomSheet
import ch.admin.foitt.wallet.feature.credentialDetail.presentation.composables.MenuBottomSheet
import ch.admin.foitt.wallet.feature.credentialDetail.presentation.composables.VisibleBottomSheet
import ch.admin.foitt.wallet.feature.deferredDetail.presentation.model.DeferredDetailUiState
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.calculateVerticalPadding
import ch.admin.foitt.wallet.platform.composables.presentation.HeightReportingLayout
import ch.admin.foitt.wallet.platform.composables.presentation.WindowWidthClass
import ch.admin.foitt.wallet.platform.composables.presentation.bottomSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.horizontalSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.layout.LazyColumn
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.composables.presentation.spaceBarKeyClickable
import ch.admin.foitt.wallet.platform.composables.presentation.topSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.windowWidthClass
import ch.admin.foitt.wallet.platform.credential.presentation.LargeCredentialCard
import ch.admin.foitt.wallet.platform.credential.presentation.mock.CredentialCardMocks
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredProgressionState
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.ActorComplianceState
import ch.admin.foitt.wallet.platform.preview.AllCompactScreensPreview
import ch.admin.foitt.wallet.platform.preview.AllLargeScreensPreview
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeferredDetailScreen(
    viewModel: DeferredDetailViewModel,
) {
    val scope = rememberCoroutineScope()

    val menuSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val deleteSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val uiMode = LocalConfiguration.current.uiMode
    LaunchedEffect(uiMode) {
        viewModel.deferredDetailUiState.refreshData()
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
                scope.hideModalSheet(deleteSheetState, onHidden = viewModel::onDeleteDeferred)
            },
        )
    }

    DeferredDetailScreenContent(
        isLoading = viewModel.isLoading.collectAsStateWithLifecycle().value,
        deferredDetail = viewModel.deferredDetailUiState.stateFlow.collectAsStateWithLifecycle().value,
        onBack = viewModel::onBack,
        onMenu = viewModel::onMenu,
        onButtonClick = viewModel::onButtonClick
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
private fun DeferredDetailScreenContent(
    isLoading: Boolean,
    deferredDetail: DeferredDetailUiState,
    windowWidthClass: WindowWidthClass = currentWindowAdaptiveInfo().windowWidthClass(),
    onBack: () -> Unit,
    onMenu: () -> Unit,
    onButtonClick: () -> Unit = {},
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(color = WalletTheme.colorScheme.surfaceContainerLow)
    ) {
        when (windowWidthClass) {
            WindowWidthClass.COMPACT -> DeferredDetailCompact(
                deferredDetail = deferredDetail,
                onBack = onBack,
                onMenu = onMenu,
                onButtonClick = onButtonClick
            )

            else -> DeferredDetailLarge(
                deferredDetail = deferredDetail,
                onBack = onBack,
                onMenu = onMenu,
                onButtonClick = onButtonClick
            )
        }
        LoadingOverlay(showOverlay = isLoading)
    }
}

@Composable
private fun BoxWithConstraintsScope.DeferredDetailCompact(
    deferredDetail: DeferredDetailUiState,
    onBack: () -> Unit,
    onMenu: () -> Unit,
    onButtonClick: () -> Unit = {},
) {
    val lazyListState = rememberLazyListState()
    WalletLayouts.LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        state = lazyListState,
        contentPadding = PaddingValues(bottom = Sizes.s04),
        useTopInsets = false,
    ) {
        item {
            CredentialWithTopBar(
                deferredDetail = deferredDetail,
                credentialMinHeight = maxHeight * 0.7f,
                onBack = onBack,
                onMenu = onMenu
            )
            Spacer(Modifier.height(Sizes.s06))
        }
        item {
            CardGenericState(
                deferredDetail.credential.deferredStatus,
                onButtonClick = onButtonClick
            )
        }
    }
}

@Composable
private fun BoxWithConstraintsScope.DeferredDetailLarge(
    deferredDetail: DeferredDetailUiState,
    onBack: () -> Unit,
    onMenu: () -> Unit,
    onButtonClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .horizontalSafeDrawing()
    ) {
        val verticalSafeDrawing = WindowInsets.safeDrawing.asPaddingValues().calculateVerticalPadding()
        CredentialWithTopBar(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .topSafeDrawing()
                .bottomSafeDrawing()
                .padding(start = Sizes.s02, bottom = Sizes.s02),
            deferredDetail = deferredDetail,
            credentialMinHeight = this@DeferredDetailLarge.maxHeight - Sizes.s02 - verticalSafeDrawing,
            onBack = onBack,
            onMenu = onMenu,
        )
        Spacer(modifier = Modifier.width(Sizes.s04))
        Box(modifier = Modifier.weight(1f)) {
            val lazyListState = rememberLazyListState()
            WalletLayouts.LazyColumn(
                state = lazyListState,
                contentPadding = PaddingValues(start = Sizes.s04, end = Sizes.s04, bottom = Sizes.s02),
            ) {
                item {
                    CardGenericState(
                        deferredDetail.credential.deferredStatus,
                        onButtonClick = onButtonClick
                    )
                }
            }
        }
    }
}

@Composable
private fun CredentialWithTopBar(
    modifier: Modifier = Modifier,
    credentialMinHeight: Dp,
    deferredDetail: DeferredDetailUiState,
    onBack: () -> Unit,
    onMenu: () -> Unit
) {
    Box(modifier = modifier) {
        var topBarHeight by remember { mutableStateOf(0.dp) }
        LargeCredentialCard(
            contentPaddingValues = PaddingValues(
                start = Sizes.s04,
                top = Sizes.s03 + topBarHeight,
                end = Sizes.s04,
                bottom = Sizes.s06,
            ),
            minHeight = credentialMinHeight,
            credentialCardState = deferredDetail.credential,
        )
        HeightReportingLayout(onContentHeightMeasured = { topBarHeight = it }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Sizes.s03)
                    .topSafeDrawing(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                BackButton(onBack)
                MenuButton(onMenu)
            }
        }
    }
}

@Composable
private fun BackButton(onBack: () -> Unit) {
    FilledIconButton(
        onClick = onBack,
        modifier = Modifier
            .size(Sizes.s10)
            .spaceBarKeyClickable(onBack)
    ) {
        Icon(
            modifier = Modifier.size(Sizes.s06),
            painter = painterResource(R.drawable.wallet_ic_back_navigation),
            contentDescription = stringResource(id = R.string.tk_global_back_alt),
            tint = WalletTheme.colorScheme.onPrimary,
        )
    }
}

@Composable
private fun MenuButton(onBack: () -> Unit) {
    FilledIconButton(
        onClick = onBack,
        modifier = Modifier
            .size(Sizes.s10)
            .spaceBarKeyClickable(onBack)
    ) {
        Icon(
            modifier = Modifier.size(Sizes.s06),
            painter = painterResource(R.drawable.wallet_ic_more_vert),
            contentDescription = stringResource(id = R.string.tk_global_moreoptions_alt),
            tint = WalletTheme.colorScheme.onPrimary,
        )
    }
}

@Composable
private fun CardGenericState(
    deferredStatus: DeferredProgressionState?,
    onButtonClick: () -> Unit = {},
) {
    when (deferredStatus) {
        DeferredProgressionState.IN_PROGRESS -> {
            CardGeneric(
                modifier = Modifier,
                title = stringResource(R.string.tk_deferredCredentialDetails_inProgress_contentTitle),
                body = stringResource(R.string.tk_deferredCredentialDetails_inProgress_contentBody),
            )
        }

        else -> {
            CardGeneric(
                modifier = Modifier,
                title = stringResource(R.string.tk_deferredCredentialDetails_invalid_contentTitle),
                body = stringResource(R.string.tk_deferredCredentialDetails_invalid_contentBody),
                buttonText = stringResource(R.string.tk_deferredCredentialDetails_invalid_button),
                onButtonClick = onButtonClick
            )
        }
    }
}

@Composable
private fun CardGeneric(
    modifier: Modifier = Modifier,
    title: String,
    body: String,
    buttonText: String? = null,
    onButtonClick: () -> Unit = {},
) = Surface(
    color = WalletTheme.colorScheme.listItemBackground,
    shape = RoundedCornerShape(Sizes.s05),
    modifier = Modifier.padding(start = Sizes.s04, end = Sizes.s04)
) {
    Row(
        modifier = modifier.padding(Sizes.s06),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            WalletTexts.TitleSmall(
                modifier = Modifier.semantics {
                    heading()
                },
                text = title
            )
            WalletTexts.LabelLarge(
                text = body
            )
            buttonText?.let {
                Spacer(modifier = Modifier.height(Sizes.s04))
                Buttons.FilledTertiary(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = buttonText,
                    onClick = onButtonClick,
                )
            }
        }
    }
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

@Composable
private fun CredentialDetailScreenPreview(windowWidthClass: WindowWidthClass) {
    DeferredDetailScreenContent(
        isLoading = false,
        deferredDetail = DeferredDetailUiState(
            credential = CredentialCardMocks.state8,
            issuer = ActorUiState(
                name = "Issuer",
                painter = painterResource(id = R.drawable.ic_swiss_cross_small),
                trustStatus = TrustStatus.TRUSTED,
                vcSchemaTrustStatus = VcSchemaTrustStatus.TRUSTED,
                actorType = ActorType.ISSUER,
                actorComplianceState = ActorComplianceState.REPORTED,
                nonComplianceReason = "report reason",
            ),
        ),
        windowWidthClass = windowWidthClass,
        onBack = {},
        onMenu = {},
        onButtonClick = {}
    )
}
