package ch.admin.foitt.wallet.app.presentation

import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.platform.appSetupState.domain.repository.OnboardingStateRepository
import ch.admin.foitt.wallet.platform.login.domain.usecase.NavigateToLogin
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StartViewModel @Inject constructor(
    private val onboardingStateRepository: OnboardingStateRepository,
    private val navigateToLogin: NavigateToLogin,
    private val navManager: NavigationManager,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {

    override val topBarState = TopBarState.None

    fun navigateToFirstScreen() {
        viewModelScope.launch {
            when (onboardingStateRepository.getOnboardingState()) {
                true -> navManager.replaceCurrentWith(navigateToLogin())
                false -> navManager.replaceCurrentWith(Destination.OnboardingIntroScreen)
            }
        }
    }
}
