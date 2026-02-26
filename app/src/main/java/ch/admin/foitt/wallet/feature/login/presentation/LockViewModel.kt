package ch.admin.foitt.wallet.feature.login.presentation

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.platform.login.domain.usecase.NavigateToLogin
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LockViewModel @Inject constructor(
    private val navManager: NavigationManager,
    private val navigateToLogin: NavigateToLogin,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState), DefaultLifecycleObserver {

    override val topBarState = TopBarState.None

    override fun onResume(owner: LifecycleOwner) {
        viewModelScope.launch {
            navManager.replaceCurrentWith(navigateToLogin())
        }
    }
}
