package ch.admin.foitt.wallet.feature.settings.presentation.biometrics

import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel(assistedFactory = EnableBiometricsLockoutViewModel.Factory::class)
class EnableBiometricsLockoutViewModel @AssistedInject constructor(
    private val navManager: NavigationManager,
    setTopBarState: SetTopBarState,
    @Assisted("errorCode") val errorCode: String,
) : ScreenViewModel(setTopBarState) {
    override val topBarState = TopBarState.None

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("errorCode") errorCode: String,
        ): EnableBiometricsLockoutViewModel
    }

    fun onClose() = navManager.popBackStackOrToRoot()
}
