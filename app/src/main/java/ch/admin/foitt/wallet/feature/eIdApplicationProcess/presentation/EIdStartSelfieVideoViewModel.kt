package ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation

import ch.admin.foitt.avwrapper.AVBeam
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel(assistedFactory = EIdStartSelfieVideoViewModel.Factory::class)
class EIdStartSelfieVideoViewModel @AssistedInject constructor(
    private val navManager: NavigationManager,
    private val avBeam: AVBeam,
    setTopBarState: SetTopBarState,
    @Assisted private val caseId: String,
) : ScreenViewModel(setTopBarState) {

    @AssistedFactory
    interface Factory {
        fun create(caseId: String): EIdStartSelfieVideoViewModel
    }

    override val topBarState = TopBarState.WithCloseButton(
        onClose = ::onClose
    )

    fun onStart() {
        navManager.navigateTo(
            Destination.EIdFaceScannerScreen(caseId = caseId)
        )
    }

    fun onClose() {
        avBeam.shutDown()
        navManager.navigateBackToHomeScreen(popUntil = Destination.EIdStartAvSessionScreen::class)
    }
}
