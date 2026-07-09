package ch.admin.foitt.wallet.feature.deferredDetail.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
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
import ch.admin.foitt.wallet.platform.composables.presentation.WindowWidthClass
import ch.admin.foitt.wallet.platform.composables.presentation.horizontalSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.layout.LazyColumn
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.composables.presentation.windowWidthClass
import ch.admin.foitt.wallet.platform.credential.presentation.CredentialCardCreditFormat
import ch.admin.foitt.wallet.platform.credential.presentation.mock.CredentialCardMocks
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredProgressionState
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
    onButtonClick: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = WalletTheme.colorScheme.surfaceContainerLow)
    ) {
        when (windowWidthClass) {
            WindowWidthClass.COMPACT -> DeferredDetailCompact(
                deferredDetail = deferredDetail,
                onButtonClick = onButtonClick,
            )

            else -> DeferredDetailLarge(
                deferredDetail = deferredDetail,
                onButtonClick = onButtonClick,
            )
        }
        LoadingOverlay(showOverlay = isLoading)
    }
}

@Composable
private fun DeferredDetailCompact(
    deferredDetail: DeferredDetailUiState,
    onButtonClick: () -> Unit = {},
) {
    WalletLayouts.LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        state = rememberLazyListState(),
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
                credentialCardState = deferredDetail.credential,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Sizes.s04),
            )
        }

        item {
            Spacer(Modifier.height(Sizes.s04))
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
private fun DeferredDetailLarge(
    deferredDetail: DeferredDetailUiState,
    onButtonClick: () -> Unit = {},
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
                credentialCardState = deferredDetail.credential,
                modifier = Modifier
                    .semantics { isTraversalGroup = true }
                    .weight(1f)
                    .padding(Sizes.s04),
            )

            WalletLayouts.LazyColumn(
                modifier = Modifier
                    .semantics { isTraversalGroup = true }
                    .weight(1f),
                state = rememberLazyListState(),
                contentPadding = PaddingValues(Sizes.s04),
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
private fun CardGenericState(
    deferredStatus: DeferredProgressionState?,
    onButtonClick: () -> Unit = {},
) {
    when (deferredStatus) {
        DeferredProgressionState.IN_PROGRESS -> {
            CardGeneric(
                title = stringResource(R.string.tk_deferredCredentialDetails_inProgress_contentTitle),
                body = stringResource(R.string.tk_deferredCredentialDetails_inProgress_contentBody),
            )
        }

        DeferredProgressionState.FAILED -> {
            CardGeneric(
                title = stringResource(R.string.tk_deferredCredentialDetails_issuanceFailed_contentTitle),
                body = stringResource(R.string.tk_deferredCredentialDetails_issuanceFailed_contentBody),
            )
        }

        DeferredProgressionState.INVALID,
        null -> {
            CardGeneric(
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
    title: String,
    body: String,
    modifier: Modifier = Modifier,
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
        onButtonClick = {}
    )
}
