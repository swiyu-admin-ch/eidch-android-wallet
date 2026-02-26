package ch.admin.foitt.wallet.platform.invitation.presentation

import androidx.lifecycle.viewModelScope
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestErrorBody
import ch.admin.foitt.openid4vc.domain.usecase.DeclinePresentation
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationErrorScreenState
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = InvitationFailureViewModel.Factory::class)
class InvitationFailureViewModel @AssistedInject constructor(
    private val navManager: NavigationManager,
    private val declinePresentation: DeclinePresentation,
    setTopBarState: SetTopBarState,
    @Assisted val invitationErrorScreenState: InvitationErrorScreenState,
    @Assisted private val uri: String?
) : ScreenViewModel(setTopBarState) {

    @AssistedFactory
    interface Factory {
        fun create(invitationErrorScreenState: InvitationErrorScreenState, uri: String?): InvitationFailureViewModel
    }

    override val topBarState = TopBarState.None

    fun close() = handleErrorType(uri)

    private fun handleErrorType(uri: String?) = viewModelScope.launch {
        when (invitationErrorScreenState) {
            InvitationErrorScreenState.EMPTY_WALLET,
            InvitationErrorScreenState.NO_COMPATIBLE_CREDENTIAL -> {
                if (uri != null) {
                    declinePresentation(url = uri, reason = PresentationRequestErrorBody.ErrorType.CLIENT_REJECTED)
                }
            }
            else -> {}
        }
        navManager.popBackStackOrToRoot()
    }
}
