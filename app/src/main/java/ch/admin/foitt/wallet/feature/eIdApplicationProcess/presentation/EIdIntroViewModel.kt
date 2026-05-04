package ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation

import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination.EIdPrivacyPolicyScreen
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EIdIntroViewModel @Inject constructor(
    private val navManager: NavigationManager,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {
    override val topBarState = TopBarState.Details(
        titleId = null,
        onUp = ::onBack,
    )

    fun onRequestEId() = navManager.navigateTo(EIdPrivacyPolicyScreen)

    fun onSkip() = navManager.popBackStackOrToRoot()

    fun onBack() = navManager.popBackStackOrToRoot()
}
