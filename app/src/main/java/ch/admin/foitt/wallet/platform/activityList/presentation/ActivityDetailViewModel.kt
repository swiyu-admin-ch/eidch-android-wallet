package ch.admin.foitt.wallet.platform.activityList.presentation

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityDetail
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.DeleteActivity
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.GetActivityDetailFlow
import ch.admin.foitt.wallet.platform.activityList.presentation.model.ActivityDetailScreenUiState
import ch.admin.foitt.wallet.platform.activityList.presentation.model.toActivityDetailUiState
import ch.admin.foitt.wallet.platform.badges.domain.model.BadgeType
import ch.admin.foitt.wallet.platform.badges.presentation.model.BadgeBottomSheetUiState
import ch.admin.foitt.wallet.platform.badges.presentation.model.toBadgeBottomSheetUiState
import ch.admin.foitt.wallet.platform.composables.presentation.adapter.GetDrawableFromImageData
import ch.admin.foitt.wallet.platform.credential.presentation.adapter.GetCredentialCardState
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.genericScreens.domain.model.GenericErrorScreenState
import ch.admin.foitt.wallet.platform.messageEvents.domain.model.ActivityEvent
import ch.admin.foitt.wallet.platform.messageEvents.domain.model.NonComplianceEvent
import ch.admin.foitt.wallet.platform.messageEvents.domain.repository.ActivityEventRepository
import ch.admin.foitt.wallet.platform.messageEvents.domain.repository.NonComplianceEventRepository
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarBackground
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.extension.refreshableStateFlow
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.openLink
import ch.admin.foitt.wallet.platform.utils.toPainter
import com.github.michaelbull.result.mapBoth
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ActivityDetailViewModel.Factory::class)
class ActivityDetailViewModel @AssistedInject constructor(
    @param:ApplicationContext private val appContext: Context,
    getActivityDetailFlow: GetActivityDetailFlow,
    private val getDrawableFromImageData: GetDrawableFromImageData,
    private val getCredentialCardState: GetCredentialCardState,
    private val deleteActivity: DeleteActivity,
    private val activityEventRepository: ActivityEventRepository,
    environmentSetupRepository: EnvironmentSetupRepository,
    private val nonComplianceEventRepository: NonComplianceEventRepository,
    private val navManager: NavigationManager,
    setTopBarState: SetTopBarState,
    @Assisted("credentialId") private val credentialId: Long,
    @Assisted("activityId") private val activityId: Long,
) : ScreenViewModel(setTopBarState) {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("credentialId") credentialId: Long,
            @Assisted("activityId") activityId: Long
        ): ActivityDetailViewModel
    }

    override val topBarState = TopBarState.Details(
        titleId = R.string.tk_activity_activityDetail_title,
        topBarBackground = TopBarBackground.CLUSTER,
        onUp = this::onBack
    )
    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    val nonComplianceEnabled = environmentSetupRepository.nonComplianceEnabled

    private val _badgeBottomSheetUiState: MutableStateFlow<BadgeBottomSheetUiState?> = MutableStateFlow(null)
    val badgeBottomSheet = _badgeBottomSheetUiState.asStateFlow()

    private val _showConfirmationBottomSheet: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val showConfirmationBottomSheet = _showConfirmationBottomSheet.asStateFlow()

    val activityDetailUiState = refreshableStateFlow(ActivityDetailScreenUiState.EMPTY) {
        getActivityDetailFlow(credentialId = credentialId, activityId = activityId)
            .map { result ->
                result.mapBoth(
                    success = { activityDetail ->
                        _isLoading.value = false
                        mapToUiState(activityDetail)
                    },
                    failure = {
                        navigateToErrorScreen()
                        null
                    }
                )
            }.filterNotNull()
    }

    private val _isSnackbarVisible = MutableStateFlow(false)
    val isSnackbarVisible = _isSnackbarVisible.asStateFlow()

    init {
        viewModelScope.launch {
            nonComplianceEventRepository.event.collect { event ->
                when (event) {
                    NonComplianceEvent.NONE -> _isSnackbarVisible.value = false
                    NonComplianceEvent.REPORT_SENT -> {
                        _isSnackbarVisible.value = true
                        delay(4000L)
                        nonComplianceEventRepository.resetEvent()
                    }
                }
            }
        }
    }

    fun hideNonComplianceSnackbar() = nonComplianceEventRepository.resetEvent()

    private suspend fun mapToUiState(activityDetail: ActivityDetail?) = when (activityDetail) {
        null -> ActivityDetailScreenUiState.EMPTY
        else -> {
            val drawable = activityDetail.activity.actorImageData?.let {
                getDrawableFromImageData(it)
            }

            ActivityDetailScreenUiState(
                activity = activityDetail.activity.toActivityDetailUiState(drawable?.toPainter()),
                credential = getCredentialCardState(activityDetail.credential).copy(status = null),
                claims = activityDetail.claims,
            )
        }
    }

    fun onBadge(badgeType: BadgeType.ActorInfoBadge) {
        _badgeBottomSheetUiState.value = badgeType.toBadgeBottomSheetUiState(
            actorName = activityDetailUiState.stateFlow.value.activity.localizedActorName,
            reason = activityDetailUiState.stateFlow.value.activity.nonComplianceReason,
            onMoreInformation = { onMoreInformation(R.string.tk_badgeInformation_furtherInformation_link_value) },
        )
    }

    fun onDismissBadgeBottomSheet() {
        _badgeBottomSheetUiState.value = null
    }

    fun onDeleteActivity() {
        _showConfirmationBottomSheet.value = true
    }

    fun onDeleteActivityConfirmed() = viewModelScope.launch {
        deleteActivity(activityId)
        activityEventRepository.setEvent(ActivityEvent.DELETED)
        onBack()
    }

    fun onDismissConfirmationBottomSheet() {
        _showConfirmationBottomSheet.value = false
    }

    fun onReportActor() {
        navManager.navigateTo(
            Destination.NonComplianceListScreen(
                activityId = activityId,
                activityType = activityDetailUiState.stateFlow.value.activity.activityType,
            )
        )
    }

    fun onBack() {
        navManager.popBackStack()
    }

    private fun onMoreInformation(@StringRes uriResource: Int) = appContext.openLink(uriResource)

    private fun navigateToErrorScreen() {
        navManager.replaceCurrentWith(Destination.GenericErrorScreen(GenericErrorScreenState.GENERIC))
    }
}
