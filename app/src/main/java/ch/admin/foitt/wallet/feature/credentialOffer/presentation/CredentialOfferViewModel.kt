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
import ch.admin.foitt.wallet.platform.appSetupState.domain.usecase.SaveFirstCredentialWasAdded
import ch.admin.foitt.wallet.platform.badges.domain.model.BadgeType
import ch.admin.foitt.wallet.platform.badges.presentation.model.BadgeBottomSheetUiState
import ch.admin.foitt.wallet.platform.badges.presentation.model.toBadgeBottomSheetUiState
import ch.admin.foitt.wallet.platform.credential.presentation.adapter.GetCredentialCardState
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.UpdateCredentialStatus
import ch.admin.foitt.wallet.platform.genericScreens.domain.model.GenericErrorScreenState
import ch.admin.foitt.wallet.platform.messageEvents.domain.model.CredentialOfferEvent
import ch.admin.foitt.wallet.platform.messageEvents.domain.repository.CredentialOfferEventRepository
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
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.utils.openLink
import com.github.michaelbull.result.annotation.UnsafeResultValueAccess
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
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
    private val deleteCredential: DeleteCredential,
    private val getActorUiState: GetActorUiState,
    getActorForScope: GetActorForScope,
    private val credentialOfferEventRepository: CredentialOfferEventRepository,
    private val saveIssuanceActivity: SaveIssuanceActivity,
    private val acceptCredential: AcceptCredential,
    private val fetchAndCacheIssuerDisplayData: FetchAndCacheIssuerDisplayData,
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

    fun onAcceptClicked() = if (credentialOfferUiState.stateFlow.value.issuer.trustStatus != TrustStatus.EXTERNAL) {
        acceptCredential()
    } else {
        _showConfirmationBottomSheet.value = true
    }

    fun acceptCredential() = viewModelScope.launch {
        acceptCredential(credentialId).onFailure {
            navigateToErrorScreen()
            return@launch
        }
        saveFirstCredentialWasAdded()
        saveIssuanceActivity(
            credentialId = credentialId,
            actorDisplayData = actorDisplayData.value,
            issuerFallbackName = appContext.getString(R.string.tk_credential_offer_issuer_name_unknown)
        )
        credentialOfferEventRepository.setEvent(CredentialOfferEvent.ACCEPTED)
        navManager.popBackStackOrToRoot()
    }

    fun onDeclineClicked() {
        navManager.navigateTo(
            Destination.DeclineCredentialOfferScreen(
                credentialId = credentialId,
            )
        )
    }

    fun onDeclineBottomSheet() = viewModelScope.launch {
        // User declined a VC from an unknown issuer. Delete immediately and navigate to home
        deleteCredential(credentialId).onFailure { error ->
            when (error) {
                is SsiError.Unexpected -> Timber.e(error.cause)
            }
        }.onSuccess {
            credentialOfferEventRepository.setEvent(CredentialOfferEvent.DECLINED)
        }
        navManager.navigateOutAndTo(DestinationGroup.CredentialOffer::class, Destination.HomeScreen)
    }

    fun onBadge(badgeType: BadgeType) {
        _badgeBottomSheetUiState.value = when (badgeType) {
            is BadgeType.ActorInfoBadge -> badgeType.toBadgeBottomSheetUiState(
                actorName = credentialOfferUiState.stateFlow.value.issuer.name ?: "",
                reason = credentialOfferUiState.stateFlow.value.issuer.nonComplianceReason,
                onMoreInformation = { onMoreInformation(R.string.tk_badgeInformation_furtherInformation_link_value) },
            )

            is BadgeType.ClaimInfoBadge -> badgeType.toBadgeBottomSheetUiState(
                onMoreInformation = { onMoreInformation(R.string.tk_badgeInformation_furtherInformation_link_value) },
            )
        }
    }

    fun onDismissBadgeBottomSheet() {
        _badgeBottomSheetUiState.value = null
    }

    fun onDismissConfirmationBottomSheet() {
        _showConfirmationBottomSheet.value = false
    }

    private fun navigateToErrorScreen() {
        navManager.replaceCurrentWith(Destination.GenericErrorScreen(GenericErrorScreenState.GENERIC))
    }

    fun onReportWrongDataClicked() {
        navManager.navigateTo(Destination.ReportWrongDataScreen)
    }

    private fun onMoreInformation(@StringRes uriResource: Int) = appContext.openLink(uriResource)
}
