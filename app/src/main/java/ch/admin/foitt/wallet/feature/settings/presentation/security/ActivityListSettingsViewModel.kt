package ch.admin.foitt.wallet.feature.settings.presentation.security

import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.settings.presentation.security.activityList.HistorySettingsBottomSheet
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.AreActivitiesEnabledFlow
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.DeleteAllActivities
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.SaveAreActivitiesEnabled
import ch.admin.foitt.wallet.platform.messageEvents.domain.model.ActivityEvent
import ch.admin.foitt.wallet.platform.messageEvents.domain.repository.ActivityEventRepository
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import com.github.michaelbull.result.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityListSettingsViewModel @Inject constructor(
    areActivitiesEnabledFlow: AreActivitiesEnabledFlow,
    private val saveAreActivitiesEnabled: SaveAreActivitiesEnabled,
    private val activityEventRepository: ActivityEventRepository,
    private val deleteAllActivities: DeleteAllActivities,
    private val navManager: NavigationManager,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {
    override val topBarState = TopBarState.Details(navManager::popBackStack, R.string.tk_settings_activityHistory_title)

    val areActivitiesEnabled = areActivitiesEnabledFlow()

    private val _visibleBottomSheet = MutableStateFlow(HistorySettingsBottomSheet.NONE)
    val visibleBottomSheet = _visibleBottomSheet.asStateFlow()

    private val _isSnackbarVisible = MutableStateFlow(false)
    val isSnackbarVisible = _isSnackbarVisible.asStateFlow()

    init {
        viewModelScope.launch {
            activityEventRepository.event.collect { event ->
                when (event) {
                    ActivityEvent.NONE -> _isSnackbarVisible.value = false
                    ActivityEvent.DELETED_ALL -> {
                        _isSnackbarVisible.value = true
                        delay(4000L)
                        activityEventRepository.resetEvent()
                    }
                    ActivityEvent.DELETED -> {} // not used on this screen
                }
            }
        }
    }

    fun onHistoryChange(enabled: Boolean) {
        if (!enabled) {
            // show confirmation bottom sheet and then save
            _visibleBottomSheet.value = HistorySettingsBottomSheet.SAVE_HISTORY
        } else {
            // do not show confirmation bottom sheet and just save enabled state
            viewModelScope.launch {
                saveAreActivitiesEnabled(enabled)
            }
        }
    }

    fun onDisableHistory() {
        onCloseBottomSheet()
        viewModelScope.launch {
            saveAreActivitiesEnabled(false)
        }
    }

    fun onDeleteAllActivities() {
        _visibleBottomSheet.value = HistorySettingsBottomSheet.DELETE_HISTORY
    }

    fun onDeleteAllActivitiesConfirmed() = viewModelScope.launch {
        onCloseBottomSheet()
        deleteAllActivities()
            .onSuccess {
                activityEventRepository.setEvent(ActivityEvent.DELETED_ALL)
            }
    }

    fun onCloseBottomSheet() {
        _visibleBottomSheet.value = HistorySettingsBottomSheet.NONE
    }

    fun onCloseSnackbar() = viewModelScope.launch {
        activityEventRepository.resetEvent()
    }
}
