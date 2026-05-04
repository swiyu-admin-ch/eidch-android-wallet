package ch.admin.foitt.wallet.feature.deferredDetail.presentation

import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.feature.credentialDetail.domain.model.IssuerDisplay
import ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase.GetCredentialIssuerDisplaysFlow
import ch.admin.foitt.wallet.feature.credentialDetail.presentation.composables.VisibleBottomSheet
import ch.admin.foitt.wallet.feature.deferredDetail.presentation.model.DeferredDetailUiState
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.composables.presentation.adapter.GetDrawableFromUri
import ch.admin.foitt.wallet.platform.credential.domain.model.DeferredCredentialDisplayData
import ch.admin.foitt.wallet.platform.credential.presentation.adapter.GetCredentialCardState
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayConst
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage
import ch.admin.foitt.wallet.platform.genericScreens.domain.model.GenericErrorScreenState
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.ActorComplianceState
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.extension.refreshableStateFlow
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteDeferred
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetDeferredCredentialWithDetailFlow
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

@HiltViewModel(assistedFactory = DeferredDetailViewModel.Factory::class)
class DeferredDetailViewModel @AssistedInject constructor(
    getDeferredCredentialDetailFlow: GetDeferredCredentialWithDetailFlow,
    getCredentialIssuerDisplaysFlow: GetCredentialIssuerDisplaysFlow,
    private val getCredentialCardState: GetCredentialCardState,
    private val getDrawableFromUri: GetDrawableFromUri,
    private val navManager: NavigationManager,
    private val deleteDeferred: DeleteDeferred,
    setTopBarState: SetTopBarState,
    @Assisted private val credentialId: Long
) : ScreenViewModel(setTopBarState) {
    @AssistedFactory
    interface Factory {
        fun create(credentialId: Long): DeferredDetailViewModel
    }

    override val topBarState = TopBarState.None

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _visibleBottomSheet = MutableStateFlow(VisibleBottomSheet.NONE)
    val visibleBottomSheet = _visibleBottomSheet.asStateFlow()

    private var isDeleting = false

    val deferredDetailUiState = refreshableStateFlow(DeferredDetailUiState.EMPTY) {
        combine(
            getDeferredCredentialDetailFlow(credentialId),
            getCredentialIssuerDisplaysFlow(credentialId)
        ) { detailsResult, issuerDisplayResult ->
            if (isDeleting) return@combine null
            when {
                detailsResult.isOk -> {
                    _isLoading.value = false
                    mapToUiState(
                        deferredDisplayData = detailsResult.get(),
                        issuerDisplay = issuerDisplayResult.get()
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
        deferredDisplayData: DeferredCredentialDisplayData?,
        issuerDisplay: IssuerDisplay?
    ) = when (deferredDisplayData) {
        null -> DeferredDetailUiState.EMPTY
        else -> DeferredDetailUiState(
            credential = getCredentialCardState(deferredDisplayData),
            issuer = issuerDisplay.toActorUiState()
        )
    }

    private fun toggleMenuBottomSheet() = when (_visibleBottomSheet.value) {
        VisibleBottomSheet.NONE -> _visibleBottomSheet.value = VisibleBottomSheet.MENU
        VisibleBottomSheet.MENU -> _visibleBottomSheet.value = VisibleBottomSheet.NONE
        else -> {}
    }

    fun onBack() {
        navManager.popBackStack()
    }

    fun onMenu() {
        toggleMenuBottomSheet()
    }

    fun onButtonClick() {
        onDelete()
    }

    fun onDelete() {
        _visibleBottomSheet.value = VisibleBottomSheet.DELETE
    }

    fun onDeleteDeferred() {
        isDeleting = true
        _visibleBottomSheet.value = VisibleBottomSheet.NONE
        viewModelScope.launch {
            deleteDeferred(credentialId).onFailure { error ->
                isDeleting = false
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
        navManager.replaceCurrentWith(Destination.GenericErrorScreen(GenericErrorScreenState.GENERIC))
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
            actorComplianceState = ActorComplianceState.UNKNOWN,
            nonComplianceReason = null,
        )
    } ?: ActorUiState.EMPTY.copy(actorType = ActorType.ISSUER)
}
