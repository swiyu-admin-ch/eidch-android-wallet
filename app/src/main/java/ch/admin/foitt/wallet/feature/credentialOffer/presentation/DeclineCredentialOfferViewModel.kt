package ch.admin.foitt.wallet.feature.credentialOffer.presentation

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.credentialOffer.presentation.model.DeclineCredentialOfferUiState
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.GetActorForScope
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.adapter.GetActorUiState
import ch.admin.foitt.wallet.platform.badges.domain.model.BadgeType
import ch.admin.foitt.wallet.platform.badges.presentation.model.BadgeBottomSheetUiState
import ch.admin.foitt.wallet.platform.badges.presentation.model.toBadgeBottomSheetUiState
import ch.admin.foitt.wallet.platform.messageEvents.domain.model.CredentialOfferEvent
import ch.admin.foitt.wallet.platform.messageEvents.domain.repository.CredentialOfferEventRepository
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.navigation.domain.model.DestinationGroup
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteCredential
import ch.admin.foitt.wallet.platform.utils.openLink
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = DeclineCredentialOfferViewModel.Factory::class)
class DeclineCredentialOfferViewModel @AssistedInject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val getActorUiState: GetActorUiState,
    private val navManager: NavigationManager,
    private val credentialOfferEventRepository: CredentialOfferEventRepository,
    getActorForScope: GetActorForScope,
    private val deleteCredential: DeleteCredential,
    @Assisted private val credentialId: Long,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {
    override val topBarState = TopBarState.None

    @AssistedFactory
    interface Factory {
        fun create(credentialId: Long): DeclineCredentialOfferViewModel
    }

    private val issuerDisplayData = getActorForScope(ComponentScope.CredentialIssuer)

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _badgeBottomSheetUiState: MutableStateFlow<BadgeBottomSheetUiState?> = MutableStateFlow(null)
    val badgeBottomSheet = _badgeBottomSheetUiState.asStateFlow()

    val uiState: StateFlow<DeclineCredentialOfferUiState> = issuerDisplayData.map { displayData ->
        val uiState = DeclineCredentialOfferUiState(
            issuer = getActorUiState(
                actorDisplayData = displayData,
            ),
        )
        _isLoading.value = false
        uiState
    }.toStateFlow(DeclineCredentialOfferUiState.EMPTY, 0)

    fun onCancel() = navManager.popBackStack()

    fun onDecline() {
        viewModelScope.launch {
            deleteCredential(credentialId = credentialId)
                .onFailure { error ->
                    when (error) {
                        is SsiError.Unexpected -> Timber.e(error.cause)
                    }
                }.onSuccess {
                    credentialOfferEventRepository.setEvent(CredentialOfferEvent.DECLINED)
                }
            navManager.navigateOutAndTo(DestinationGroup.CredentialOffer::class, Destination.HomeScreen)
        }
    }

    fun onBadge(badgeType: BadgeType) {
        _badgeBottomSheetUiState.value = when (badgeType) {
            is BadgeType.ActorInfoBadge -> badgeType.toBadgeBottomSheetUiState(
                actorName = uiState.value.issuer.name ?: "",
                reason = uiState.value.issuer.nonComplianceReason,
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
