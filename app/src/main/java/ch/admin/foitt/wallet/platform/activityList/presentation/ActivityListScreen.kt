package ch.admin.foitt.wallet.platform.activityList.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityType
import ch.admin.foitt.wallet.platform.activityList.presentation.composables.activityListItem
import ch.admin.foitt.wallet.platform.activityList.presentation.composables.emptyHistoryActivityListItem
import ch.admin.foitt.wallet.platform.activityList.presentation.model.ActivityUiState
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.ToastAnimated
import ch.admin.foitt.wallet.platform.composables.presentation.horizontalSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.layout.LazyColumn
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.scaffold.presentation.LocalScaffoldPaddings
import ch.admin.foitt.wallet.platform.utils.OnPauseEventHandler
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun ActivityListScreen(viewModel: ActivityListViewModel) {
    OnPauseEventHandler {
        viewModel.hideActivityDeletedSnackbar()
    }

    ActivityListScreenContent(
        isLoading = viewModel.isLoading.collectAsStateWithLifecycle().value,
        isSnackbarVisible = viewModel.isSnackbarVisible.collectAsStateWithLifecycle().value,
        activities = viewModel.activities.collectAsStateWithLifecycle().value,
        onActivity = viewModel::onActivity,
        onCloseSnackbar = viewModel::hideActivityDeletedSnackbar,
    )
}

@Composable
fun ActivityListScreenContent(
    isLoading: Boolean,
    isSnackbarVisible: Boolean,
    activities: List<ActivityUiState>,
    onActivity: (Long) -> Unit,
    onCloseSnackbar: () -> Unit,
) = Box(
    modifier = Modifier
        .fillMaxSize()
        .background(color = WalletTheme.colorScheme.surfaceContainerLow)
) {
    WalletLayouts.LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalSafeDrawing(),
        state = rememberLazyListState(),
        contentPadding = PaddingValues(start = Sizes.s04, top = Sizes.s02, end = Sizes.s04, bottom = Sizes.s04),
        useTopInsets = false,
    ) {
        item {
            WalletLayouts.TopInsetSpacer(
                shouldScrollUnderTopBar = true,
                scaffoldPaddings = LocalScaffoldPaddings.current,
            )
        }

        if (activities.isEmpty()) {
            emptyHistoryActivityListItem()
        } else {
            activities.forEachIndexed { index, activity ->
                activityListItem(
                    activity = activity,
                    isFirstItem = activities.indices.first == index,
                    isLastItem = activities.lastIndex == index,
                    onClick = onActivity
                )
            }
        }
    }
    ToastAnimated(
        isVisible = isSnackbarVisible,
        isSnackBarDesign = true,
        messageToast = R.string.tk_activity_activityList_entryDeleted_title,
        iconEnd = R.drawable.wallet_ic_cross,
        onCloseToast = onCloseSnackbar,
    )
    LoadingOverlay(isLoading)
}

@WalletAllScreenPreview
@Composable
private fun ActivityListScreenPreview() {
    WalletTheme {
        ActivityListScreenContent(
            isLoading = false,
            isSnackbarVisible = true,
            activities = listOf(
                ActivityUiState(
                    id = 1,
                    activityType = ActivityType.ISSUANCE,
                    date = "01.01.2025 12:34",
                    localizedActorName = "Preview Issuer",
                ),
                ActivityUiState(
                    id = 2,
                    activityType = ActivityType.PRESENTATION_ACCEPTED,
                    date = "01.01.2025 12:35",
                    localizedActorName = "Preview Verifier",
                ),
                ActivityUiState(
                    id = 3,
                    activityType = ActivityType.PRESENTATION_DECLINED,
                    date = "01.01.2025 12:36",
                    localizedActorName = "Preview Verifier",
                )
            ),
            onActivity = {},
            onCloseSnackbar = {},
        )
    }
}
