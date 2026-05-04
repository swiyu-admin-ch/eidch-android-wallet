package ch.admin.foitt.wallet.feature.settings.presentation.security

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.settings.presentation.composables.ButtonSettingsItem
import ch.admin.foitt.wallet.feature.settings.presentation.composables.SettingsCard
import ch.admin.foitt.wallet.feature.settings.presentation.composables.SwitchSettingsItem
import ch.admin.foitt.wallet.feature.settings.presentation.security.activityList.DeleteHistoryBottomSheet
import ch.admin.foitt.wallet.feature.settings.presentation.security.activityList.HistorySettingsBottomSheet
import ch.admin.foitt.wallet.feature.settings.presentation.security.activityList.SaveHistoryBottomSheet
import ch.admin.foitt.wallet.platform.composables.ToastAnimated
import ch.admin.foitt.wallet.platform.composables.presentation.addTopScaffoldPadding
import ch.admin.foitt.wallet.platform.composables.presentation.bottomSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.horizontalSafeDrawing
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletListItems
import ch.admin.foitt.wallet.theme.WalletTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityListSettingsScreen(viewModel: ActivityListSettingsViewModel) {
    val scope = rememberCoroutineScope()

    val historySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val deleteSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val visibleBottomSheet = viewModel.visibleBottomSheet.collectAsStateWithLifecycle().value
    when (visibleBottomSheet) {
        HistorySettingsBottomSheet.NONE -> {}
        HistorySettingsBottomSheet.SAVE_HISTORY -> SaveHistoryBottomSheet(
            sheetState = historySheetState,
            onDismiss = {
                scope.hideModalSheet(historySheetState, onHidden = viewModel::onCloseBottomSheet)
            },
            onCancel = {
                scope.hideModalSheet(historySheetState, onHidden = viewModel::onCloseBottomSheet)
            },
            onConfirm = {
                scope.hideModalSheet(historySheetState, onHidden = viewModel::onDisableHistory)
            },
        )

        HistorySettingsBottomSheet.DELETE_HISTORY -> DeleteHistoryBottomSheet(
            sheetState = deleteSheetState,
            onDismiss = {
                scope.hideModalSheet(historySheetState, onHidden = viewModel::onCloseBottomSheet)
            },
            onCancel = {
                scope.hideModalSheet(historySheetState, onHidden = viewModel::onCloseBottomSheet)
            },
            onDelete = {
                scope.hideModalSheet(historySheetState, onHidden = viewModel::onDeleteAllActivitiesConfirmed)
            },
        )
    }

    ActivityListSettingsScreenContent(
        areActivitiesEnabled = viewModel.areActivitiesEnabled.collectAsStateWithLifecycle().value,
        isSnackbarVisible = viewModel.isSnackbarVisible.collectAsStateWithLifecycle().value,
        onHistoryChange = viewModel::onHistoryChange,
        onDeleteEntireHistory = viewModel::onDeleteAllActivities,
        onCloseSnackbar = viewModel::onCloseSnackbar,
    )
}

@Composable
private fun ActivityListSettingsScreenContent(
    areActivitiesEnabled: Boolean,
    isSnackbarVisible: Boolean,
    onHistoryChange: (Boolean) -> Unit,
    onDeleteEntireHistory: () -> Unit,
    onCloseSnackbar: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WalletTheme.colorScheme.surfaceContainerLow)
            .addTopScaffoldPadding()
            .verticalScroll(rememberScrollState())
            .horizontalSafeDrawing()
            .bottomSafeDrawing()
    ) {
        SettingsCard {
            WalletListItems.SwitchSettingsItem(
                title = stringResource(R.string.tk_settings_activityHistory_deletion_toggleHistory_primary),
                subtitle = stringResource(R.string.tk_settings_activityHistory_deletion_toggleHistory_secondary),
                leadingIcon = R.drawable.wallet_ic_save_disk,
                isSwitchChecked = areActivitiesEnabled,
                onSwitchChange = onHistoryChange,
            )
        }

        Spacer(modifier = Modifier.height(Sizes.s06))

        SettingsCard {
            WalletListItems.ButtonSettingsItem(
                title = stringResource(R.string.tk_settings_activityHistory_deletion_primary),
                leadingIcon = R.drawable.wallet_ic_trashcan,
                contentColor = WalletTheme.colorScheme.onLightError,
                onClick = onDeleteEntireHistory,
            )
        }
    }
    ToastAnimated(
        isVisible = isSnackbarVisible,
        isSnackBarDesign = true,
        messageToast = R.string.tk_settings_activityHistory_deletion_successMessage,
        iconEnd = R.drawable.wallet_ic_cross,
        onCloseToast = onCloseSnackbar,
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

@WalletAllScreenPreview
@Composable
private fun ActivityListSettingsScreenContentPreview() {
    WalletTheme {
        ActivityListSettingsScreenContent(
            areActivitiesEnabled = true,
            isSnackbarVisible = true,
            onHistoryChange = {},
            onDeleteEntireHistory = {},
            onCloseSnackbar = {},
        )
    }
}
