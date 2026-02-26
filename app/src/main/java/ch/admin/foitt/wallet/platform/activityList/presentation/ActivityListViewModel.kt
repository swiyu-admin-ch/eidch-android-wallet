package ch.admin.foitt.wallet.platform.activityList.presentation

import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.GetActivitiesWithDisplaysFlow
import ch.admin.foitt.wallet.platform.activityList.presentation.model.toActivityUiState
import ch.admin.foitt.wallet.platform.composables.presentation.adapter.GetDrawableFromImageData
import ch.admin.foitt.wallet.platform.messageEvents.domain.model.ActivityEvent
import ch.admin.foitt.wallet.platform.messageEvents.domain.repository.ActivityEventRepository
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarBackground
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.toPainter
import com.github.michaelbull.result.mapBoth
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ActivityListViewModel.Factory::class)
class ActivityListViewModel @AssistedInject constructor(
    getActivitiesWithDisplaysFlow: GetActivitiesWithDisplaysFlow,
    private val getDrawableFromImageData: GetDrawableFromImageData,
    private val activityEventRepository: ActivityEventRepository,
    private val navManager: NavigationManager,
    setTopBarState: SetTopBarState,
    @Assisted private val credentialId: Long
) : ScreenViewModel(setTopBarState) {

    @AssistedFactory
    interface Factory {
        fun create(credentialId: Long): ActivityListViewModel
    }

    override val topBarState = TopBarState.Details(
        titleId = R.string.tk_activity_activityList_title,
        topBarBackground = TopBarBackground.CLUSTER,
        onUp = this::onBack
    )

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    val activities = getActivitiesWithDisplaysFlow(credentialId)
        .map { result ->
            result.mapBoth(
                success = { activitiesWithDisplays ->
                    _isLoading.value = false
                    activitiesWithDisplays.map { activityDisplayData ->
                        val drawable = activityDisplayData.actorImageData?.let {
                            getDrawableFromImageData(it)
                        }

                        activityDisplayData.toActivityUiState(drawable?.toPainter())
                    }
                },
                failure = {
                    navigateToErrorScreen()
                    emptyList()
                }
            )
        }.toStateFlow(emptyList())

    private val _isSnackbarVisible = MutableStateFlow(false)
    val isSnackbarVisible = _isSnackbarVisible.asStateFlow()

    init {
        viewModelScope.launch {
            activityEventRepository.event.collect { event ->
                when (event) {
                    ActivityEvent.NONE -> _isSnackbarVisible.value = false
                    ActivityEvent.DELETED -> {
                        _isSnackbarVisible.value = true
                        delay(4000L)
                        activityEventRepository.resetEvent()
                    }
                }
            }
        }
    }

    fun hideActivityDeletedSnackbar() = activityEventRepository.resetEvent()

    fun onBack() {
        navManager.popBackStack()
    }

    fun onActivity(activityId: Long) {
        navManager.navigateTo(
            Destination.ActivityDetailScreen(
                credentialId = credentialId,
                activityId = activityId
            )
        )
    }

    private fun navigateToErrorScreen() {
        navManager.replaceCurrentWith(Destination.GenericErrorScreen)
    }
}
