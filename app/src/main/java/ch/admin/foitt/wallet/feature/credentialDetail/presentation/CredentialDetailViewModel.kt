package ch.admin.foitt.wallet.feature.credentialDetail.presentation

import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.credentialDetail.domain.model.IssuanceType
import ch.admin.foitt.wallet.feature.credentialDetail.domain.model.IssuerDisplay
import ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase.GetCredentialIssuanceType
import ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase.GetCredentialIssuerDisplaysFlow
import ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase.HasExhaustedBatchCopies
import ch.admin.foitt.wallet.feature.credentialDetail.presentation.composables.VisibleBottomSheet
import ch.admin.foitt.wallet.feature.credentialDetail.presentation.model.CredentialDetailUiState
import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityType
import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityWithActorDisplayData
import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityStateRepository
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.GetActivitiesWithDisplaysFlow
import ch.admin.foitt.wallet.platform.activityList.presentation.model.toActivityUiState
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorMetadataDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.composables.presentation.adapter.GetDrawableFromImageData
import ch.admin.foitt.wallet.platform.credential.presentation.adapter.GetCredentialCardState
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.UpdateCredentialStatus
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.genericScreens.domain.model.GenericErrorScreenState
import ch.admin.foitt.wallet.platform.messageEvents.domain.model.CredentialEvent
import ch.admin.foitt.wallet.platform.messageEvents.domain.repository.CredentialEventRepository
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceReportingData
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarAction
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarBackground
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.extension.refreshableStateFlow
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialDetail
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteCredential
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetCredentialDetailFlow
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ActorComplianceState
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus
import ch.admin.foitt.wallet.platform.utils.UiString
import ch.admin.foitt.wallet.platform.utils.toPainter
import com.github.michaelbull.result.get
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = CredentialDetailViewModel.Factory::class)
@Suppress("TooManyFunctions")
class CredentialDetailViewModel @AssistedInject constructor(
    getCredentialDetailFlow: GetCredentialDetailFlow,
    getCredentialIssuerDisplaysFlow: GetCredentialIssuerDisplaysFlow,
    getActivitiesWithDisplaysFlow: GetActivitiesWithDisplaysFlow,
    activityStateRepository: ActivityStateRepository,
    environmentSetupRepository: EnvironmentSetupRepository,
    private val getCredentialCardState: GetCredentialCardState,
    private val getCredentialIssuanceType: GetCredentialIssuanceType,
    private val updateCredentialStatus: UpdateCredentialStatus,
    private val getDrawableFromImageData: GetDrawableFromImageData,
    private val navManager: NavigationManager,
    private val deleteCredential: DeleteCredential,
    private val credentialEventRepository: CredentialEventRepository,
    private val hasExhaustedBatchCopies: HasExhaustedBatchCopies,
    private val setTopBarState: SetTopBarState,
    @Assisted private val credentialId: Long
) : ScreenViewModel(setTopBarState) {

    @AssistedFactory
    interface Factory {
        fun create(credentialId: Long): CredentialDetailViewModel
    }

    override val topBarState: TopBarState
        get() {
            val topBarTitle = credentialDetailUiState.stateFlow.value.credential.title?.let { title ->
                UiString.Dynamic(title)
            }

            return TopBarState.Custom(
                title = topBarTitle,
                onUp = ::onBack,
                topBarBackground = TopBarBackground.CLUSTER,
                actions = listOf(
                    TopBarAction(
                        onClick = ::onMenu,
                        icon = R.drawable.wallet_ic_more_vert,
                        contentDescription = R.string.tk_global_moreoptions_alt,
                    )
                ),
                onAXDown = ::tryEmitFocusEvents
            )
        }

    private val _focusEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val focusEvents = _focusEvents.asSharedFlow()

    private fun tryEmitFocusEvents() {
        _focusEvents.tryEmit(Unit)
    }

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _visibleBottomSheet = MutableStateFlow(VisibleBottomSheet.NONE)
    val visibleBottomSheet = _visibleBottomSheet.asStateFlow()

    val areActivitiesEnabled = activityStateRepository.areActivitiesEnabledFlow()

    val nonComplianceEnabled = environmentSetupRepository.nonComplianceEnabled

    private val issuanceType = flow {
        emit(getCredentialIssuanceType(credentialId).get())
    }.toStateFlow(null)
    private val allActivities = MutableStateFlow<List<ActivityWithActorDisplayData>>(emptyList())
    private val issuerDisplay = MutableStateFlow<IssuerDisplay?>(null)

    val credentialDetailUiState = refreshableStateFlow(CredentialDetailUiState.EMPTY) {
        combine(
            getCredentialDetailFlow(credentialId),
            getCredentialIssuerDisplaysFlow(credentialId),
            getActivitiesWithDisplaysFlow(credentialId),
            areActivitiesEnabled,
            issuanceType,
        ) { detailsResult, issuerDisplayResult, activitiesResult, areActivitiesEnabled, issuanceType ->
            when {
                detailsResult.isOk -> {
                    _isLoading.value = false
                    val activities = activitiesResult.get() ?: emptyList()
                    allActivities.value = activities
                    val display = issuerDisplayResult.get()
                    issuerDisplay.value = display

                    mapToUiState(
                        credentialDetail = detailsResult.get(),
                        issuerDisplay = display,
                        areActivitiesEnabled = areActivitiesEnabled,
                        activities = activities,
                        issuanceType = issuanceType,
                    )
                }

                else -> {
                    navigateToErrorScreen()
                    null
                }
            }
        }.filterNotNull()
    }

    private suspend fun mapToUiState(
        credentialDetail: CredentialDetail?,
        issuerDisplay: IssuerDisplay?,
        areActivitiesEnabled: Boolean,
        activities: List<ActivityWithActorDisplayData>,
        issuanceType: IssuanceType?,
    ) = when (credentialDetail) {
        null -> CredentialDetailUiState.EMPTY
        else -> CredentialDetailUiState(
            credential = getCredentialCardState(credentialDetail.credential),
            clusterItems = credentialDetail.clusterItems,
            issuer = issuerDisplay.toActorUiState(),
            issuanceType = issuanceType,
            areActivitiesEnabled = areActivitiesEnabled,
            activities = activities.take(2).map { activityDisplayData ->
                activityDisplayData.toActivityUiState()
            },
            showBatchWarning = hasExhaustedBatchCopies(credentialId),
        )
    }

    private fun toggleMenuBottomSheet() = when (_visibleBottomSheet.value) {
        VisibleBottomSheet.NONE -> _visibleBottomSheet.value = VisibleBottomSheet.MENU
        VisibleBottomSheet.MENU -> _visibleBottomSheet.value = VisibleBottomSheet.NONE
        else -> {}
    }

    init {
        viewModelScope.launch {
            updateCredentialStatus(credentialId)
        }
    }

    fun setTitle() {
        setTopBarState(topBarState)
    }

    fun onBack() {
        navManager.popBackStack()
    }

    fun onMenu() {
        toggleMenuBottomSheet()
    }

    fun onDelete() {
        _visibleBottomSheet.value = VisibleBottomSheet.DELETE
    }

    fun onDeleteCredential() {
        _visibleBottomSheet.value = VisibleBottomSheet.NONE
        viewModelScope.launch {
            deleteCredential(credentialId = credentialId)
                .onOk {
                    credentialEventRepository.setEvent(CredentialEvent.DELETED)
                }.onErr { error ->
                    when (error) {
                        is SsiError.Unexpected -> Timber.e(error.cause)
                    }
                }
            navManager.popBackStackTo(Destination.HomeScreen::class, false)
        }
    }

    fun onBottomSheetDismiss() {
        _visibleBottomSheet.value = VisibleBottomSheet.NONE
    }

    private fun navigateToErrorScreen() {
        navManager.replaceCurrentWith(Destination.GenericErrorScreen(GenericErrorScreenState.Offer.generic()))
    }

    fun onUpdate() {
        navManager.navigateTo(Destination.UpdateCredentialScreen(credentialId))
        onBottomSheetDismiss()
    }

    fun onIssuanceInfo() {
        val destination = when (issuanceType.value) {
            IssuanceType.BATCH -> Destination.BatchIssuanceInfoScreen(credentialId)
            IssuanceType.STANDARD -> Destination.IssuanceInfoScreen
            null -> return
        }
        navManager.navigateTo(destination)
    }

    fun onReportIssuer() {
        val activityId = allActivities.value
            .find { it.activityType == ActivityType.ISSUANCE }?.activityId ?: return
        navManager.navigateTo(
            Destination.NonComplianceListScreen(
                reportingData = NonComplianceReportingData(
                    actorDisplayData = ActorMetadataDisplayData(
                        activityId = activityId,
                        localizedActorName = issuerDisplay.value?.name ?: "",
                        actorImageData = issuerDisplay.value?.image
                    ),
                    rawData = null,
                    issuerDid = null,
                    activityType = ActivityType.ISSUANCE
                ),
            )
        )
    }

    fun onEntireHistory() {
        navManager.navigateTo(
            Destination.ActivityListScreen(
                credentialId = credentialId
            )
        )
    }

    fun onActivitySettings() {
        navManager.navigateTo(Destination.SecuritySettingsScreen)
    }

    private suspend fun IssuerDisplay?.toActorUiState() = this?.let {
        ActorUiState(
            name = name,
            painter = getDrawableFromImageData(image)?.toPainter(),
            trustStatus = TrustStatus.UNKNOWN,
            vcSchemaTrustStatus = VcSchemaTrustStatus.UNPROTECTED,
            actorType = ActorType.ISSUER,
            actorComplianceState = ActorComplianceState.UNKNOWN,
            nonComplianceReason = null,
        )
    } ?: ActorUiState.EMPTY.copy(actorType = ActorType.ISSUER)
}
