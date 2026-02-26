package ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation

import ch.admin.foitt.avwrapper.AVBeam
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.SetHasLegalGuardian
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EIdGuardianshipViewModel @Inject constructor(
    private val avBeam: AVBeam,
    private val navManager: NavigationManager,
    private val setHasLegalGuardian: SetHasLegalGuardian,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {
    override val topBarState = TopBarState.DetailsWithCloseButton(
        titleId = null,
        onUp = {
            shutDownLibraryAndGoBack()
        },
        onClose = {
            avBeam.shutDown()
            navManager.navigateBackToHomeScreen(Destination.EIdIntroScreen::class)
        },
    )

    fun onDeclareGuardianship(hasGuardianship: Boolean) {
        setHasLegalGuardian(hasGuardianship)
        navManager.navigateTo(Destination.EIdDocumentSelectionScreen)
    }

    fun shutDownLibraryAndGoBack() {
        avBeam.shutDown()
        navManager.popBackStack()
    }
}
