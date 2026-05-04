package ch.admin.foitt.wallet.feature.presentationRequest.presentation

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.GetPresentationRequestCredentialListFlow
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.model.PresentationCredentialListUiState
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.FetchAndCacheVerifierDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.GetActorForScope
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.adapter.GetActorUiState
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.badges.domain.model.BadgeType
import ch.admin.foitt.wallet.platform.badges.presentation.model.BadgeBottomSheetUiState
import ch.admin.foitt.wallet.platform.badges.presentation.model.toBadgeBottomSheetUiState
import ch.admin.foitt.wallet.platform.credential.presentation.adapter.GetCredentialCardState
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestWithRaw
import ch.admin.foitt.wallet.platform.genericScreens.domain.model.GenericErrorScreenState
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.extension.refreshableStateFlow
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.openLink
import com.github.michaelbull.result.mapBoth
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = PresentationCredentialListViewModel.Factory::class)
class PresentationCredentialListViewModel @AssistedInject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val navManager: NavigationManager,
    getPresentationRequestCredentialListFlow: GetPresentationRequestCredentialListFlow,
    private val fetchAndCacheVerifierDisplayData: FetchAndCacheVerifierDisplayData,
    private val getCredentialCardState: GetCredentialCardState,
    private val getActorUiState: GetActorUiState,
    getActorForScope: GetActorForScope,
    setTopBarState: SetTopBarState,
    @Assisted private val compatibleCredentials: Set<CompatibleCredential>,
    @Assisted private val presentationRequestWithRaw: PresentationRequestWithRaw,
) : ScreenViewModel(setTopBarState) {

    @AssistedFactory
    interface Factory {
        fun create(
            compatibleCredentials: Set<CompatibleCredential>,
            presentationRequestWithRaw: PresentationRequestWithRaw,
        ): PresentationCredentialListViewModel
    }

    override val topBarState = TopBarState.None

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _badgeBottomSheetUiState: MutableStateFlow<BadgeBottomSheetUiState?> = MutableStateFlow(null)
    val badgeBottomSheet = _badgeBottomSheetUiState.asStateFlow()

    private val verifierDisplayData = getActorForScope(ComponentScope.Verifier)
    val verifierUiState = verifierDisplayData.map { verifierDisplayData ->
        getActorUiState(
            actorDisplayData = verifierDisplayData,
        )
    }.toStateFlow(ActorUiState.EMPTY, 0)

    val presentationCredentialListUiState = refreshableStateFlow(PresentationCredentialListUiState.EMPTY) {
        getPresentationRequestCredentialListFlow(
            compatibleCredentials = compatibleCredentials,
        ).map { result ->
            result.mapBoth(
                success = { presentationCredentialListUi ->
                    _isLoading.value = false
                    PresentationCredentialListUiState(
                        credentials = presentationCredentialListUi.credentials.map { getCredentialCardState(it) },
                    )
                },
                failure = {
                    navigateToErrorScreen()
                    null
                },
            )
        }.filterNotNull()
    }

    init {
        viewModelScope.launch {
            updateVerifierDisplayData()
        }
    }

    fun onCredentialSelected(credentialId: Long) {
        val compatibleCredential = compatibleCredentials.find { it.credentialId == credentialId }
        compatibleCredential?.let {
            navManager.replaceCurrentWith(
                destination = Destination.PresentationRequestScreen(
                    compatibleCredential = compatibleCredential,
                    presentationRequestWithRaw = presentationRequestWithRaw,
                )
            )
        } ?: navigateToErrorScreen()
    }

    fun onBack() = navManager.popBackStackOrToRoot()

    private suspend fun updateVerifierDisplayData() {
        fetchAndCacheVerifierDisplayData(
            authorizationRequest = presentationRequestWithRaw.authorizationRequest,
        )
    }

    private fun navigateToErrorScreen() {
        navManager.replaceCurrentWith(Destination.GenericErrorScreen(GenericErrorScreenState.GENERIC))
    }

    fun onBadge(badgeType: BadgeType) {
        _badgeBottomSheetUiState.value = when (badgeType) {
            is BadgeType.ActorInfoBadge -> badgeType.toBadgeBottomSheetUiState(
                actorName = verifierUiState.value.name ?: "",
                reason = verifierUiState.value.nonComplianceReason,
                onMoreInformation = { onMoreInformation(R.string.tk_badgeInformation_furtherInformation_link_value) },
            )

            is BadgeType.ClaimInfoBadge -> badgeType.toBadgeBottomSheetUiState(
                onMoreInformation = { onMoreInformation(R.string.tk_badgeInformation_furtherInformation_link_value) },
            )
        }
    }

    fun onDismissBottomSheet() {
        _badgeBottomSheetUiState.value = null
    }

    private fun onMoreInformation(@StringRes uriResource: Int) = appContext.openLink(uriResource)
}
