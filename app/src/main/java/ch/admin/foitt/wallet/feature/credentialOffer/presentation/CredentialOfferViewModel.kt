package ch.admin.foitt.wallet.feature.credentialOffer.presentation

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.credentialOffer.domain.model.CredentialOffer
import ch.admin.foitt.wallet.feature.credentialOffer.domain.usecase.AcceptCredential
import ch.admin.foitt.wallet.feature.credentialOffer.domain.usecase.GetCredentialOfferFlow
import ch.admin.foitt.wallet.feature.credentialOffer.presentation.model.CredentialOfferUiState
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.SaveIssuanceActivity
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.FetchAndCacheIssuerDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.GetActorForScope
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.adapter.GetActorUiState
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.toBadgeBottomSheetUiState
import ch.admin.foitt.wallet.platform.appSetupState.domain.usecase.SaveFirstCredentialWasAdded
import ch.admin.foitt.wallet.platform.badges.presentation.model.BadgeBottomSheetUiState
import ch.admin.foitt.wallet.platform.credential.presentation.adapter.GetCredentialCardState
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.UpdateCredentialStatus
import ch.admin.foitt.wallet.platform.genericScreens.domain.model.GenericErrorScreenState
import ch.admin.foitt.wallet.platform.messageEvents.domain.model.CredentialEvent
import ch.admin.foitt.wallet.platform.messageEvents.domain.repository.CredentialEventRepository
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.navigation.domain.model.DestinationGroup
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.extension.refreshableStateFlow
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteCredential
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ActorComplianceState
import ch.admin.foitt.wallet.platform.utils.openLink
import com.github.michaelbull.result.annotation.UnsafeResultValueAccess
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = CredentialOfferViewModel.Factory::class)
class CredentialOfferViewModel @AssistedInject constructor(
    @param:ApplicationContext private val appContext: Context,
    getCredentialOfferFlow: GetCredentialOfferFlow,
    private val navManager: NavigationManager,
    private val updateCredentialStatus: UpdateCredentialStatus,
    private val getCredentialCardState: GetCredentialCardState,
    private val saveFirstCredentialWasAdded: SaveFirstCredentialWasAdded,
    private val getActorUiState: GetActorUiState,
    getActorForScope: GetActorForScope,
    private val credentialEventRepository: CredentialEventRepository,
    private val saveIssuanceActivity: SaveIssuanceActivity,
    private val acceptCredential: AcceptCredential,
    private val fetchAndCacheIssuerDisplayData: FetchAndCacheIssuerDisplayData,
    private val deleteCredential: DeleteCredential,
    @Assisted private val credentialId: Long,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {
    override val topBarState = TopBarState.None

    @AssistedFactory
    interface Factory {
        fun create(credentialId: Long): CredentialOfferViewModel
    }

    private val _badgeBottomSheetUiState: MutableStateFlow<BadgeBottomSheetUiState?> = MutableStateFlow(null)
    val badgeBottomSheet = _badgeBottomSheetUiState.asStateFlow()

    private val _showConfirmationBottomSheet: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val showConfirmationBottomSheet = _showConfirmationBottomSheet.asStateFlow()

    private val actorDisplayData = getActorForScope(ComponentScope.CredentialIssuer)

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    @OptIn(UnsafeResultValueAccess::class)
    val credentialOfferUiState = refreshableStateFlow(initialData = CredentialOfferUiState.EMPTY) {
        combine(
            getCredentialOfferFlow(credentialId),
            actorDisplayData,
        ) { credentialOfferResult, actorDisplayData ->
            when {
                credentialOfferResult.isOk -> {
                    _isLoading.value = false
                    mapToUiState(
                        credentialOffer = credentialOfferResult.value,
                        actorDisplayData = actorDisplayData,
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
        credentialOffer: CredentialOffer?,
        actorDisplayData: ActorDisplayData,
    ) = when (credentialOffer) {
        null -> CredentialOfferUiState.EMPTY
        else -> CredentialOfferUiState(
            issuer = getActorUiState(actorDisplayData),
            credential = getCredentialCardState(credentialOffer.credential),
            claims = credentialOffer.claims,
        )
    }

    init {
        viewModelScope.launch {
            launch { updateCredentialStatus(credentialId) }
            launch { fetchAndCacheIssuerDisplayData(credentialId) }
        }
    }

    fun onAcceptClicked() = when (actorDisplayData.value.actorComplianceState) {
        ActorComplianceState.REPORTED -> _showConfirmationBottomSheet.value = true
        else -> acceptCredential()
    }

    fun acceptCredential() = viewModelScope.launch {
        acceptCredential(credentialId).onErr {
            navigateToErrorScreen()
            return@launch
        }
        saveFirstCredentialWasAdded()
        saveIssuanceActivity(
            credentialId = credentialId,
            actorDisplayData = actorDisplayData.value,
            issuerFallbackName = appContext.getString(R.string.tk_credential_offer_issuer_name_unknown)
        )
        credentialEventRepository.setEvent(CredentialEvent.ACCEPTED)
        navManager.popBackStackOrToRoot()
    }

    fun onDeclineClicked() {
        navManager.navigateTo(
            Destination.DeclineCredentialOfferScreen(
                credentialId = credentialId,
            )
        )
    }

    fun onDeclineCredential() = viewModelScope.launch {
        deleteCredential(credentialId = credentialId)
            .onErr { error ->
                when (error) {
                    is SsiError.Unexpected -> Timber.e(error.cause)
                }
            }.onOk {
                credentialEventRepository.setEvent(CredentialEvent.DECLINED)
            }
        navManager.navigateOutAndTo(DestinationGroup.CREDENTIAL_OFFER, Destination.HomeScreen)
    }

    fun onDismissBadgeBottomSheet() {
        _badgeBottomSheetUiState.value = null
    }

    fun onDismissConfirmationBottomSheet() {
        _showConfirmationBottomSheet.value = false
    }

    fun onActorNameTap() {
        _badgeBottomSheetUiState.value = credentialOfferUiState.stateFlow.value.issuer.toBadgeBottomSheetUiState {
            onMoreInformation(R.string.tk_badgeInformation_furtherInformation_link_value)
        }
    }

    fun onReportedActorInfo() {
        _badgeBottomSheetUiState.value = BadgeBottomSheetUiState.NonCompliantActor(
            actorName = credentialOfferUiState.stateFlow.value.issuer.name ?: "",
            actorPainter = credentialOfferUiState.stateFlow.value.issuer.painter,
            reason = credentialOfferUiState.stateFlow.value.issuer.nonComplianceReason,
            onMoreInformation = { onMoreInformation(R.string.tk_badgeInformation_furtherInformation_link_value) },
        )
    }

    private fun navigateToErrorScreen() {
        navManager.replaceCurrentWith(Destination.GenericErrorScreen(GenericErrorScreenState.Offer.generic()))
    }

    private fun onMoreInformation(@StringRes uriResource: Int) = appContext.openLink(uriResource)
}
