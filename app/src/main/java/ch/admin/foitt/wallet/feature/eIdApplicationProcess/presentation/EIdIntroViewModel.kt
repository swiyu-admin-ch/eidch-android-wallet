package ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation

import android.content.Context
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination.EIdNotSupportedDeviceScreen
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination.EIdPrivacyPolicyScreen
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.hasGyroscope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class EIdIntroViewModel @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val navManager: NavigationManager,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {
    override val topBarState = TopBarState.Details(
        titleId = null,
        onUp = ::onBack,
    )

    val hasGyroscope = appContext.hasGyroscope()

    fun onRequestEId() {
        if (hasGyroscope) {
            navManager.navigateTo(EIdPrivacyPolicyScreen)
        } else {
            navManager.navigateTo(EIdNotSupportedDeviceScreen)
        }
    }

    fun onSkip() = navManager.popBackStackOrToRoot()

    fun onBack() = navManager.popBackStackOrToRoot()
}
