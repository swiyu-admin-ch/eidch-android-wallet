package ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation

import ch.admin.foitt.avwrapper.AVBeam
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdDocumentType
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.SetDocumentType
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EIdDocumentSelectionViewModel @Inject constructor(
    private val avBeam: AVBeam,
    private val navManager: NavigationManager,
    private val setDocumentType: SetDocumentType,
    environmentSetupRepository: EnvironmentSetupRepository,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {
    override val topBarState = TopBarState.DetailsWithCloseButton(
        titleId = null,
        onUp = navManager::popBackStack,
        onClose = {
            avBeam.shutDown()
            navManager.navigateBackToHomeScreen(popUntil = Destination.EIdIntroScreen::class)
        }
    )

    val showEIdMockMrzButton = environmentSetupRepository.eIdMockMrzEnabled

    fun onDocumentSelected(documentType: EIdDocumentType) {
        setDocumentType(documentType)
        navManager.navigateTo(Destination.EIdDocumentScannerInfoScreen(caseId = ""))
    }

    fun onClickMock() {
        navManager.navigateTo(Destination.MrzChooserScreen)
    }
}
