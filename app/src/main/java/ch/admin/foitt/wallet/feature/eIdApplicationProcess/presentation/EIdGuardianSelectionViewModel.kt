package ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation

import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel(assistedFactory = EIdGuardianSelectionViewModel.Factory::class)
class EIdGuardianSelectionViewModel @AssistedInject constructor(
    private val navManager: NavigationManager,
    setTopBarState: SetTopBarState,
    @Assisted private val caseId: String,
) : ScreenViewModel(setTopBarState) {
    @AssistedFactory
    interface Factory {
        fun create(caseId: String): EIdGuardianSelectionViewModel
    }

    override val topBarState = TopBarState.DetailsWithCloseButton(
        titleId = null,
        onUp = navManager::popBackStack,
        onClose = {
            navManager.navigateBackToHomeScreen(Destination.EIdIntroScreen::class)
        }
    )

    fun onObtainConsent() = navManager.navigateTo(
        Destination.EIdGuardianConsentScreen(caseId = caseId)
    )

    fun onContinueAsGuardian() = navManager.navigateTo(
        Destination.EIdGuardianVerificationScreen(caseId = caseId)
    )
}
