package ch.admin.foitt.wallet.feature.onboarding.presentation

import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingActivityViewModel @Inject constructor(
    private val navManager: NavigationManager,
    setTopBarState: SetTopBarState,
) : OnboardingViewModel(setTopBarState) {
    override val topBarState = TopBarState.Details(onUp = ::onBack, titleId = null, onAXDown = { tryEmitFocusEvents() })

    fun onNext() = navManager.navigateTo(Destination.OnboardingPresentScreen)
    fun onBack() = navManager.popBackStack()
}
