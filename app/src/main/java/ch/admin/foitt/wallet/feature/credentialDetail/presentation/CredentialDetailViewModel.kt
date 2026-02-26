package ch.admin.foitt.wallet.feature.credentialDetail.presentation

import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.feature.credentialDetail.domain.model.IssuerDisplay
import ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase.GetCredentialIssuerDisplaysFlow
import ch.admin.foitt.wallet.feature.credentialDetail.presentation.composables.VisibleBottomSheet
import ch.admin.foitt.wallet.feature.credentialDetail.presentation.model.CredentialDetailUiState
import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityDisplayData
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.GetActivitiesWithDisplaysFlow
import ch.admin.foitt.wallet.platform.activityList.presentation.model.toActivityUiState
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.composables.presentation.adapter.GetDrawableFromImageData
import ch.admin.foitt.wallet.platform.composables.presentation.adapter.GetDrawableFromUri
import ch.admin.foitt.wallet.platform.credential.presentation.adapter.GetCredentialCardState
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.UpdateCredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayConst
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceState
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.extension.refreshableStateFlow
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialDetail
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteCredential
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetCredentialDetailFlow
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus
import ch.admin.foitt.wallet.platform.utils.toPainter
import com.github.michaelbull.result.get
import com.github.michaelbull.result.onFailure
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = CredentialDetailViewModel.Factory::class)
class CredentialDetailViewModel @AssistedInject constructor(
    getCredentialDetailFlow: GetCredentialDetailFlow,
    getCredentialIssuerDisplaysFlow: GetCredentialIssuerDisplaysFlow,
    getActivitiesWithDisplaysFlow: GetActivitiesWithDisplaysFlow,
    private val getCredentialCardState: GetCredentialCardState,
    private val updateCredentialStatus: UpdateCredentialStatus,
    private val getDrawableFromUri: GetDrawableFromUri,
    private val getDrawableFromImageData: GetDrawableFromImageData,
    private val navManager: NavigationManager,
    private val deleteCredential: DeleteCredential,
    setTopBarState: SetTopBarState,
    @Assisted private val credentialId: Long
) : ScreenViewModel(setTopBarState) {

    @AssistedFactory
    interface Factory {
        fun create(credentialId: Long): CredentialDetailViewModel
    }

    override val topBarState = TopBarState.None

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _visibleBottomSheet = MutableStateFlow(VisibleBottomSheet.NONE)
    val visibleBottomSheet = _visibleBottomSheet.asStateFlow()

    val credentialDetailUiState = refreshableStateFlow(CredentialDetailUiState.EMPTY) {
        combine(
            getCredentialDetailFlow(credentialId),
            getCredentialIssuerDisplaysFlow(credentialId),
            getActivitiesWithDisplaysFlow(credentialId),
        ) { detailsResult, issuerDisplayResult, activitiesResult ->
            when {
                detailsResult.isOk -> {
                    _isLoading.value = false
                    mapToUiState(
                        credentialDetail = detailsResult.get(),
                        issuerDisplay = issuerDisplayResult.get(),
                        activities = activitiesResult.get() ?: emptyList(),
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
        activities: List<ActivityDisplayData>,
    ) = when (credentialDetail) {
        null -> CredentialDetailUiState.EMPTY
        else -> CredentialDetailUiState(
            credential = getCredentialCardState(credentialDetail.credential),
            clusterItems = credentialDetail.clusterItems,
            issuer = issuerDisplay.toActorUiState(),
            activities = activities.take(2).map { activityDisplayData ->
                val drawable = activityDisplayData.actorImageData?.let {
                    getDrawableFromImageData(it)
                }

                activityDisplayData.toActivityUiState(drawable?.toPainter())
            },
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
            deleteCredential(credentialId = credentialId).onFailure { error ->
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
        navManager.replaceCurrentWith(Destination.GenericErrorScreen)
    }

    fun onWrongData() {
        navManager.navigateTo(Destination.CredentialDetailWrongDataScreen)
        onBottomSheetDismiss()
    }

    fun onEntireHistory() {
        navManager.navigateTo(
            Destination.ActivityListScreen(
                credentialId = credentialId
            )
        )
    }

    private suspend fun IssuerDisplay?.toActorUiState() = this?.let {
        ActorUiState(
            name = if (locale == DisplayLanguage.FALLBACK) {
                null
            } else if (name == DisplayConst.ISSUER_FALLBACK_NAME) {
                null
            } else {
                name
            },
            painter = getDrawableFromUri(image)?.toPainter(),
            trustStatus = TrustStatus.UNKNOWN,
            vcSchemaTrustStatus = VcSchemaTrustStatus.UNPROTECTED,
            actorType = ActorType.ISSUER,
            nonComplianceState = NonComplianceState.UNKNOWN,
            nonComplianceReason = null,
        )
    } ?: ActorUiState.EMPTY.copy(actorType = ActorType.ISSUER)
}
